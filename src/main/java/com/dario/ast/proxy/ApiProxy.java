package com.dario.ast.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class ApiProxy {

    private final RestTemplate restTemplate;

    public ApiResponse makeRequest(ApiRequest request) {
        try {
            var uriBuilder = UriComponentsBuilder.fromUriString(request.uri())
                    .queryParams(request.queryParams())
                    .buildAndExpand(request.uriVariables());

            var response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    request.httpMethod(),
                    new HttpEntity<>(request.requestBody(), request.headers()),
                    String.class);
            var statusCode = (HttpStatus) response.getStatusCode();

            return new ApiResponse(statusCode, statusCode.getReasonPhrase());
        } catch (HttpStatusCodeException e) {
            var statusCode = (HttpStatus) e.getStatusCode();
            return new ApiResponse(statusCode, statusCode.getReasonPhrase());
        } catch (Exception e) {
            return new ApiResponse(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
