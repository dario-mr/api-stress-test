package com.dario.ast.core.service;

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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiService {

    private final RestTemplate restTemplate;

    public HttpStatus makeRequest(HttpMethod httpMethod, String uri,
                                  MultiValueMap<String, String> headers,
                                  MultiValueMap<String, String> params) {
        var uriBuilder = UriComponentsBuilder.fromUriString(uri)
                .queryParams(params);

        try {
            var response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    httpMethod,
                    new HttpEntity<>(headers),
                    String.class);
            return (HttpStatus) response.getStatusCode();
        } catch (HttpStatusCodeException e) {
            return (HttpStatus) e.getStatusCode();
        } catch (Exception e) {
            return INTERNAL_SERVER_ERROR;
        }
    }
}
