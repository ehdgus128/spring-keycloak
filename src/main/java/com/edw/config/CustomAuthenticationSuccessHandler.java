package com.edw.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 로그인 성공 후 사용자 정보 가져오기
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        // 클라이언트 등록 정보 가져오기
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("external");

        // OAuth2AuthorizedClientProvider를 사용하여 OAuth2AuthorizedClientManager 초기화
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .password()
                .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        // OAuth2AuthorizeRequest 생성
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("external")
                .principal(authentication)
                .attribute(HttpServletRequest.class.getName(), request)
                .attribute(HttpServletResponse.class.getName(), response)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
            String refreshTokenValue = authorizedClient.getRefreshToken().getTokenValue();

            // accessToken을 세션에 저장
            request.getSession().setAttribute("accessToken", accessTokenValue);
            // refreshToken을 세션에 저장
            request.getSession().setAttribute("refreshToken", refreshTokenValue);

            // 액세스 토큰을 포함하여 리다이렉트
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/login?error");
        }
    }
}