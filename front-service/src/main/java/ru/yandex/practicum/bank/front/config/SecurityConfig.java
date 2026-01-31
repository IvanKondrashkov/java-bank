package ru.yandex.practicum.bank.front.config;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/**", "/logout/**", "/oauth2/**", "/error").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("{baseUrl}");
        return handler;
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mapped = new HashSet<>();
            for (GrantedAuthority authority : authorities) {
                mapped.add(authority);
                if (authority instanceof OidcUserAuthority oidcAuthority) {
                    mapped.addAll(extractRealmRoles(oidcAuthority.getIdToken().getClaims()));
                    if (oidcAuthority.getUserInfo() != null) {
                        mapped.addAll(extractRealmRoles(oidcAuthority.getUserInfo().getClaims()));
                    }
                } else if (authority instanceof OAuth2UserAuthority oauth2Authority) {
                    mapped.addAll(extractRealmRoles(oauth2Authority.getAttributes()));
                }
            }
            return mapped;
        };
    }

    private List<SimpleGrantedAuthority> extractRealmRoles(Map<String, Object> claims) {
        if (claims == null) {
            return List.of();
        }

        Object realmAccess = claims.get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }        Object roles = realmAccessMap.get("roles");
        if (!(roles instanceof Collection<?> roleNames)) {
            return List.of();
        }
        return roleNames.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
