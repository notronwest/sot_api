package com.getstructure.mergedev

import grails.gorm.transactions.Transactional
import org.springframework.http.*
import org.springframework.web.client.*

@Transactional
class ApiService {
    private final RestTemplate restTemplate = new RestTemplate()
    private final String bearerToken
    private final String accountToken

    String get(String url) {
        HttpHeaders headers = new HttpHeaders()
        headers.setBearerAuth(bearerToken)
        headers.add("x-account-token", accountToken)

        HttpEntity<String> request = new HttpEntity<>(headers)

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class)
        return response.getBody()
    }
}

