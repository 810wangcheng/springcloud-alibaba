package com.cy.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * @author Administrator
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        if(token == null ||token.isEmpty()){
            ServerHttpResponse response = exchange.getResponse();
            HashMap<String, Object> responseMap = Maps.newHashMap();
            responseMap.put("code",401);
            responseMap.put("message","非法请求");
            responseMap.put("cause","token is empty");

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                byte[] bytes = objectMapper.writeValueAsBytes(responseMap);
                DataBuffer wrap = response.bufferFactory().wrap(bytes);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
                return response.writeWith(Mono.just(wrap));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
