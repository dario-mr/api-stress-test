package com.dario.ast.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiProxy {

    private final RestTemplate restTemplate;

    // TODO add support for request body
    public ApiResponse makeRequest(String uri,
                                   HttpMethod httpMethod,
                                   MultiValueMap<String, String> headers,
                                   Map<String, String> uriVariables,
                                   MultiValueMap<String, String> queryParams) {
        try {
            var uriBuilder = UriComponentsBuilder.fromUriString(uri)
                    .queryParams(queryParams)
                    .buildAndExpand(uriVariables);
            log.info("URI: {}", uriBuilder.toUriString());

            var response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    httpMethod,
                    new HttpEntity<>(headers),
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
