package com.dario.ast.core.domain;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "requestId")
public final class ConfigParams {

  private Long requestId;
  private String requestName;
  private Long userId;

  private String uri;
  private HttpMethod method;
  private Map<String, RequestHeader> headers;
  private Map<String, RequestUriVariable> uriVariables;
  private Map<String, RequestQueryParam> queryParams;
  private String requestBody;
}
