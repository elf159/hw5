package com.example.converterapp.services;

import com.example.converterapp.config.ConverterConfigEnv;
import com.example.converterapp.dto.ConvertResponse;

import generated.RatesResposne;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import converter.ConverterServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

import generated.Currency;

@GrpcService
public class ConverterServiceGrpcServer extends ConverterServiceGrpc.ConverterServiceImplBase {

    private final RestTemplate restTemplate;
    private final ConverterConfigEnv converterConfigEnv;
    private final RatesService ratesService;


    @Autowired
    public ConverterServiceGrpcServer(RestTemplate restTemplate, ConverterConfigEnv converterConfigEnv, RatesService ratesService) {
        this.restTemplate = restTemplate;
        this.ratesService = ratesService;
        this.restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        this.converterConfigEnv = converterConfigEnv;
    }

    @Override
    public void convert(converter.ConvertRequest request, StreamObserver<converter.ConvertResponse> responseObserver) {
        Currency first = Currency.fromValue(request.getFrom());
        Currency second = Currency.fromValue(request.getTo());
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());
        ConvertResponse convertResponse = processConvert(first, second, amount, ratesService.takeTokenAndPerform());
        converter.ConvertResponse response = converter.ConvertResponse
                .newBuilder()
                .setCurrency(convertResponse.getCurrency())
                .setAmount(convertResponse.getAmount().doubleValue()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    private ConvertResponse processConvert(Currency first, Currency second, BigDecimal amount, RatesResposne ratesResposne) {
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
