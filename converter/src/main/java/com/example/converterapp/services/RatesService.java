package com.example.converterapp.services;

import com.example.converterapp.config.ConverterConfigEnv;
import com.example.converterapp.dto.RequestTokenAnswer;
import com.example.converterapp.exception.RatesException;
import com.example.converterapp.generated.RatesResposne;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.Objects;

@Service
public class RatesService {
    private final ConverterConfigEnv converterConfigEnv;
    private final RestTemplate restTemplate;

    public RatesService(ConverterConfigEnv converterConfigEnv, RestTemplate restTemplate) {
        this.converterConfigEnv = converterConfigEnv;
        this.restTemplate = restTemplate;
    }

    @Retryable(retryFor = {RatesException.class, RestClientException.class, ConnectException.class }, maxAttempts = 4, backoff = @Backoff(delay = 50, multiplier = 2, maxDelay = 150))
    public RatesResposne takeTokenAndPerform() {
        String tokenForRates = takeToken();
        return sendRequest(tokenForRates);
    }

    private RatesResposne sendRequest(String tokenForRates) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenForRates);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<RatesResposne> resp = restTemplate.exchange(converterConfigEnv.getRATES_URL() + "/rates",
                HttpMethod.GET,
                entity,
                RatesResposne.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RatesException("Didn't get the rate");
        }
        return resp.getBody();
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
}
