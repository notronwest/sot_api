package com.getstructure.mergedev
import org.springframework.util.*
import org.springframework.http.*
import org.springframework.web.client.*

class OAuth2Service {
    private final String clientId
    private final String clientSecret
    private final String tokenUrl
    private final RestTemplate restTemplate = new RestTemplate()

    OAuth2Service(String clientId, String clientSecret, String tokenUrl) {
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.tokenUrl = tokenUrl
    }

    String getAccessToken() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        headers.setBasicAuth(clientId, clientSecret)

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>()
        map.add("grant_type", "client_credentials")

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers)

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request , String.class )
        return response.getBody()
    }
}

