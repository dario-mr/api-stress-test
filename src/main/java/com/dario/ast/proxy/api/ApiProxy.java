package com.dario.ast.proxy.api;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.dario.ast.proxy.api.dto.ApiRequest;
import com.dario.ast.proxy.api.dto.ApiResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ApiProxy {

  private final RestTemplate restTemplate;

  public ApiResponse makeRequest(ApiRequest request) {
    var start = Instant.now();

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

      return new ApiResponse(randomId(), statusCode, null, response.getBody(), elapsedTimeMs(start));
    } catch (HttpStatusCodeException e) {
      var statusCode = (HttpStatus) e.getStatusCode();
      return new ApiResponse(randomId(), statusCode, e.getResponseBodyAsString(), null, elapsedTimeMs(start));
    } catch (Exception e) {
      return new ApiResponse(randomId(), INTERNAL_SERVER_ERROR, e.getMessage(), null, elapsedTimeMs(start));
    }
  }

  private long elapsedTimeMs(Instant start) {
    return Duration.between(start, Instant.now()).toMillis();
  }

  private String randomId() {
    return UUID.randomUUID().toString();
  }
}
