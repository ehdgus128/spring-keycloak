package com.edw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${spring.security.oauth2.client.registration.external.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.external.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public KeycloakService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
                return httpResponse.getStatusCode().is4xxClientError() ||
                        httpResponse.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(ClientHttpResponse httpResponse) throws IOException {
                // 에러 처리 로직
            }
        });
    }

    public String getAccessToken(String username, String password) {
        String url = "http://172.30.1.54:8080/realms/external/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("username", username);
        body.put("password", password);
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // 응답에서 토큰 추출
            // 응답 본문이 JSON 형식으로 'access_token' 필드를 포함하는 경우
            return response.getBody(); // 필요에 따라 수정
        } else {
            // 에러 처리
            return null;
        }
    }
}
