package com.edw.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Controller
public class KeycloakController {

    @Value("${keycloak.realm}")
    private String realm;

    @GetMapping(path = "/")
    public String index(Model model, HttpSession session) {

        // 세션에서 액세스 토큰 가져오기
        String accessToken = (String) session.getAttribute("accessToken");

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
        System.out.println("accessToken : " + accessToken);

        model.addAttribute("name", user.getAttribute("name"));
        model.addAttribute("email", user.getAttribute("email"));
        model.addAttribute("accessToken", accessToken);
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

    @GetMapping("/getUserInfo")
    public ResponseEntity<String> getUserInfo(HttpSession session) {

        // office
//        String url = "http://172.30.1.54:8080/admin/realms/external/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=";
        // home
        String url = "http://localhost:8080/admin/realms/external/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=";

        HttpHeaders headers = new HttpHeaders();

        // 세션에서 액세스 토큰 가져오기
        String accessToken = (String) session.getAttribute("accessToken");

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
    public ResponseEntity<String> unlockUser(@PathVariable String userId, HttpSession session) {

        // office
//        String url = String.format("http://172.30.1.54:8080/admin/realms/external/attack-detection/brute-force/users/%s", userId);
        // home
        String url = String.format("http://localhost:8080/admin/realms/external/attack-detection/brute-force/users/%s", userId);

        HttpHeaders headers = new HttpHeaders();

        // 세션에서 액세스 토큰 가져오기
        String accessToken = (String) session.getAttribute("accessToken");
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
}
