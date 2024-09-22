package com.example.accounts.services;

import com.example.accounts.config.AccountsConfigEnv;
import com.example.accounts.dto.MessageDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OutBoxService {

    private final RestTemplate restTemplate;
    private final AccountsConfigEnv accountsConfigEnv;
    public OutBoxService(RestTemplate restTemplate, AccountsConfigEnv accountsConfigEnv) {
        this.restTemplate = restTemplate;
        this.accountsConfigEnv = accountsConfigEnv;
    }
    public boolean outBoxSendMessage(MessageDTO message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(message, headers);

        var responseEntity = restTemplate.exchange(
                accountsConfigEnv.getNOTIFICATION_SERVICE_URL() + "/notification",
                HttpMethod.POST,
                request,
                Void.class);

        return responseEntity.getStatusCode().is2xxSuccessful();
    }
}
