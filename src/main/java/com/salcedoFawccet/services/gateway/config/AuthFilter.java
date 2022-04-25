package com.salcedoFawccet.services.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private WebClient.Builder webClient;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (new URLsNoAuthNeeded().validateURL(exchange.getRequest().getURI().getPath())) return chain.filter(exchange);
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                //throw new RuntimeException("Missing auth information");
                return this.onError(exchange,"api-key missing",HttpStatus.FORBIDDEN);
            }
            String authHeader = exchange.getRequest().getHeaders ().get(HttpHeaders.AUTHORIZATION).get(0);

            String[] parts = authHeader.split(" ");

            if (parts.length != 2 || !"Bearer".equals(parts[0])) {
                throw new RuntimeException("Incorrect auth structure");
            }
            return  webClient.build().get()
                    .uri("http://auth-service/auth/authorization")
                    .header(HttpHeaders.AUTHORIZATION,exchange.getRequest().getHeaders ().get(HttpHeaders.AUTHORIZATION).get(0))
                    .exchangeToMono(response -> {
                        if (response.statusCode().equals(HttpStatus.OK)) {
                            return chain.filter(exchange);
                        }
                        else {
                            // Turn to error
                            return this.onError(exchange,"api-key missing",HttpStatus.FORBIDDEN);
                        }
                    });
        });
    }

    public static class Config {
        // Put the configuration properties
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus)  {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

}
