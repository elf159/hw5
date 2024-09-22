package com.example.converterapp.services;

import com.example.converterapp.config.ConverterConfigEnv;
import com.example.converterapp.dto.ConvertResponse;
import com.example.converterapp.dto.RequestTokenAnswer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import generated.RatesResposne;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import converter.ConverterServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import generated.Currency;

@GrpcService
public class ConverterServiceGrpcServer extends ConverterServiceGrpc.ConverterServiceImplBase {

    private final RestTemplate restTemplate;
    private final ConverterConfigEnv converterConfigEnv;

    @Autowired
    public ConverterServiceGrpcServer(RestTemplate restTemplate, ConverterConfigEnv converterConfigEnv) {
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        this.converterConfigEnv = converterConfigEnv;
    }

    @Override
    public void convert(converter.ConvertRequest request, StreamObserver<converter.ConvertResponse> responseObserver) {
        Currency first = Currency.fromValue(request.getFrom());
        Currency second = Currency.fromValue(request.getTo());
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());
        RatesResposne ratesResposne = takeTokenAndSend();
        ConvertResponse convertResponse = convert(first, second, amount, ratesResposne);
        converter.ConvertResponse response = converter.ConvertResponse
                .newBuilder()
                .setCurrency(convertResponse.getCurrency())
                .setAmount(convertResponse.getAmount().doubleValue()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    private RatesResposne takeTokenAndSend() {
        String tokenForRates = takeToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenForRates);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String jsonResponse;
        jsonResponse = restTemplate.exchange(converterConfigEnv.getRATES_URL() + "/rates",
                HttpMethod.GET,
                entity,
                String.class).getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        RatesResposne ratesResposne = null;
        try {
            ratesResposne = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ratesResposne;
    }
    private String takeToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        requestBody.add("grant_type", "client_credentials");
        requestBody.add("client_id", converterConfigEnv.getClient_id());
        requestBody.add("client_secret", converterConfigEnv.getClient_secret());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<RequestTokenAnswer> responseEntity = restTemplate.exchange(
                converterConfigEnv.getKEYCLOAK_URL() + "/realms/" + converterConfigEnv.getKEYCLOAK_REALM() + "/protocol/openid-connect/token",
                HttpMethod.POST,
                entity,
                RequestTokenAnswer.class
        );
        return Objects.requireNonNull(responseEntity.getBody()).getAccessToken();
    }

    private ConvertResponse convert(Currency first, Currency second, BigDecimal amount, RatesResposne ratesResposne) {
        ConvertResponse convertResponse = new ConvertResponse();
        try {
            if (!first.getValue().equals("RUB") && !second.getValue().equals("RUB")) {
                BigDecimal firstCount = ratesResposne.getRates().get(first.getValue()).multiply(amount);
                BigDecimal result = firstCount.divide(ratesResposne.getRates().get(second.getValue()), 2, RoundingMode.HALF_EVEN);
                convertResponse.setCurrency(second.getValue());
                convertResponse.setAmount(result.setScale(2, RoundingMode.HALF_EVEN));
            } else if (first.getValue().equals("RUB") && second.getValue().equals("RUB")) {
                convertResponse.setCurrency(second.getValue());
                convertResponse.setAmount(amount.setScale(2, RoundingMode.HALF_EVEN));
            } else {
                if (first.getValue().equals("RUB")) {
                    convertResponse.setCurrency(second.getValue());
                    convertResponse.setAmount(amount.divide(ratesResposne.getRates().get(second.getValue()), 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN));
                } else {
                    convertResponse.setCurrency(second.getValue());
                    convertResponse.setAmount(ratesResposne.getRates().get(first.getValue()).multiply(amount).setScale(2, RoundingMode.HALF_EVEN));
                }
            }
            return convertResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
