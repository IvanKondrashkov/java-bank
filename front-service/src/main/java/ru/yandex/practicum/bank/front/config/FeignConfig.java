package ru.yandex.practicum.bank.front.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientService authorizedClientService) {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
                String clientRegistrationId = oauth2Token.getAuthorizedClientRegistrationId();
                String principalName = oauth2Token.getName();
                
                OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                        clientRegistrationId, principalName);
                
                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                    String tokenValue = authorizedClient.getAccessToken().getTokenValue();
                    requestTemplate.header("Authorization", "Bearer " + tokenValue);
                }
            }
        };
    }
}