package com.edw.controller;

import com.edw.dto.UserLoginCountDTO;
import com.edw.service.ClientService;
import com.edw.service.EventEntityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
public class KeycloakController {

    @Autowired
    private ClientService clientService;
    @Autowired
    private EventEntityService eventEntityService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${keycloak.realm}")
    private String realm;

    @GetMapping("/")
    public String dashboard(Model model) {

        List<Object[]> eventCounts = eventEntityService.getEventCountsByHour("resource-server");
        model.addAttribute("eventCounts", eventCounts);
        model.addAttribute("clientList", clientService.getClientIds());

        try {
            String clientLoginCountsJson = objectMapper.writeValueAsString(eventEntityService.getClientLoginCounts());
            model.addAttribute("clientLoginCounts", clientLoginCountsJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("clientLoginCounts", "[]");
        }

        try {
            String userLoginCountsJson = objectMapper.writeValueAsString(eventEntityService.getUserLoginCounts());
            model.addAttribute("userLoginCountsJson", userLoginCountsJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("userLoginCountsJson", "[]");
        }

        try {
            String loginErrorsJson = objectMapper.writeValueAsString(eventEntityService.getLoginErrors());
            model.addAttribute("loginErrorsJson", loginErrorsJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("loginErrorsJson", "[]");
        }

        return "dashboard";
    }


    @GetMapping(path = "/user")
    public String index() {
        return "user";
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
        String url = "http://172.30.1.130:8080/admin/realms/external/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=";
        // home
//        String url = "http://localhost:8080/admin/realms/external/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=";

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
        String url = String.format("http://172.30.1.130:8080/admin/realms/external/attack-detection/brute-force/users/%s", userId);
        // home
//        String url = String.format("http://localhost:8080/admin/realms/external/attack-detection/brute-force/users/%s", userId);

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

    @GetMapping("/getUserSessions")
    public ResponseEntity<String> getSessionInfo(HttpSession session) {

        // office
        String url = "http://172.30.1.130:8080/admin/realms/external/ui-ext/sessions?first=0&max=11&type=REGULAR&search=";

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

    @GetMapping("/getOfflineSessions")
    public ResponseEntity<String> getOfflineSessions(HttpSession session) {

        // office
        String url = "http://172.30.1.130:8080/admin/realms/external/ui-ext/sessions?first=0&max=11&type=OFFLINE&search=";

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
}
