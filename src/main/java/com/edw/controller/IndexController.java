package com.edw.controller;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Objects;

@Controller
public class IndexController {

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
            System.out.println("Role: " + roleName);
        }

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

        String requestBody = "grant_type=password&username=admin&password=admin&client_id=admin-cli&client_secret=QXY4gnIEVOUSZtWN0vsA7nMxKieHH4km";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("response : " + response);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Objects.requireNonNull(e.getMessage()));
        }
    }

}

