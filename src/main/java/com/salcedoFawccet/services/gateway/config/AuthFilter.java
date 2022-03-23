package com.salcedoFawccet.services.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders. AUTHORIZATION)) {
                throw new RuntimeException("Missing auth information");
            }
            String authHeader = exchange.getRequest ().getHeaders ().get(HttpHeaders. AUTHORIZATION).get(0);

            String[] parts = authHeader.split(" ");

            if (parts.length != 2 || !"Bearer".equals(parts[0])) {
                throw new RuntimeException("Incorrect auth structure");
            }

            HttpEntity<String> entity = new HttpEntity<String>("parameters", exchange.getRequest().getHeaders());
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:3002/auth/user/authorization";

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            }catch (HttpClientErrorException e){
                return this.onError(exchange,"api-key missing",HttpStatus.FORBIDDEN);
            }

            return chain.filter(exchange);
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
