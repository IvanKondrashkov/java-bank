package ru.yandex.practicum.bank.gateway.config;

import reactor.core.publisher.Mono;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Component
public class JwtTokenRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    public JwtTokenRelayGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->
                extractToken(exchange)
                        .flatMap(token -> chain.filter(addToken(exchange, token)))
                        .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }

    private Mono<String> extractToken(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken().getTokenValue());
    }

    private ServerWebExchange addToken(ServerWebExchange exchange, String token) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("Authorization", "Bearer " + token)
                        .build())
                .build();
    }
}