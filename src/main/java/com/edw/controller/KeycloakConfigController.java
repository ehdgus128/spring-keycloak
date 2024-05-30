package com.edw.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KeycloakConfigController {

    @Value("${spring.security.oauth2.client.registration.external.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.external.client-secret}")
    private String clientSecret;

    @GetMapping("/keycloak-config")
    public Map<String, String> getKeycloakConfig(HttpSession session) {

        // 세션에서 refreshToken 가져오기
        String refreshToken = (String) session.getAttribute("refreshToken");

        Map<String, String> config = new HashMap<>();
        config.put("client_id", clientId);
        config.put("client_secret", clientSecret);
        config.put("refreshToken", refreshToken);

        return config;
    }
}