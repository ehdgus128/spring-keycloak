package com.edw.controller;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public class KeycloakController {

    @GetMapping(path = "/")
    public String index(Model model) {
        OAuth2User user = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 인증 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체에서 권한 목록 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 각 권한을 반복하여 역할 이름 가져오기
        for (GrantedAuthority authority : authorities) {
            String roleName = authority.getAuthority();
        }

        System.out.println("user.getAttribute(\"name\") : " + user.getAttribute("name"));
        System.out.println("user.getAttribute(\"email\") : " + user.getAttribute("email"));

        model.addAttribute("name", user.getAttribute("name"));
        model.addAttribute("email", user.getAttribute("email"));
        return "index";
    }

    @GetMapping(path = "/unauthenticated")
    public String unauthenticatedRequests() {
        return "unauthenticated";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/getAccessToken")
    public ResponseEntity<String> getAccessToken() {
        String url = "http://localhost:8080/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // office
//        String requestBody = "grant_type=password&username=admin&password=admin&client_id=admin-cli&client_secret=ypvI6Cvr1WfRWehHfDGZ6o3dz76UpoN3";
        // home
        String requestBody = "grant_type=password&username=admin&password=admin&client_id=admin-cli&client_secret=P5RqOALzM0n4JNgMCfORUewtajmSTKP0";


        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("response : " + response);

            // JSON 응답에서 access token만 추출하여 반환합니다.
            String accessToken = extractAccessToken(response.getBody());
            return ResponseEntity.status(response.getStatusCode()).body(accessToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Objects.requireNonNull(e.getMessage()));
        }
    }

    // Access token만 추출하는 메서드
    private String extractAccessToken(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    @GetMapping("/getUserInfo")
    public ResponseEntity<String> getUserInfo() {
        String url = "http://localhost:8080/admin/realms/external/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=";

        HttpHeaders headers = new HttpHeaders();

        // getAccessToken 메서드를 호출하여 access token을 가져옵니다.
        ResponseEntity<String> accessTokenResponse = getAccessToken();
        if (accessTokenResponse.getStatusCode() != HttpStatus.OK) {
            // Access token을 가져오는 데 실패한 경우 에러 페이지를 반환합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching access token");
        }

        // Access token을 헤더에 추가합니다.
        String accessToken = accessTokenResponse.getBody();
        headers.setBearerAuth(accessToken);

        // HTTP 요청을 보냅니다.
        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            System.out.println("response: " + response);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user info");
        }
    }

    @DeleteMapping("/unlockUser/{userId}")
    public ResponseEntity<String> unlockUser(@PathVariable String userId) {

        String url = String.format("http://localhost:8080/admin/realms/external/attack-detection/brute-force/users/%s", userId);

        HttpHeaders headers = new HttpHeaders();

        // getAccessToken 메서드를 호출하여 access token을 가져옵니다.
        ResponseEntity<String> accessTokenResponse = getAccessToken();
        if (accessTokenResponse.getStatusCode() != HttpStatus.OK) {
            // Access token을 가져오는 데 실패한 경우 에러 페이지를 반환합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching access token");
        }

        // Access token을 헤더에 추가합니다.
        String accessToken = accessTokenResponse.getBody();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.ok("User unlocked successfully");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to unlock user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error unlocking user");
        }
    }

    @GetMapping("/getUserDetails")
    public ResponseEntity<Map<String, String>> getUserDetails() {
        OAuth2User user = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("name", user.getAttribute("name"));
        userDetails.put("email", user.getAttribute("email"));
        return ResponseEntity.ok(userDetails);
    }
}
