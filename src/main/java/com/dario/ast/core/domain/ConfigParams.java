package com.dario.ast.core.domain;

import static com.dario.ast.util.MapUtil.convertToMultiValueMap;
import static com.dario.ast.util.MapUtil.flatEntryValueMap;

import com.dario.ast.proxy.ApiRequest;
import com.dario.ast.util.jackson.HttpMethodDeserializer;
import com.dario.ast.util.jackson.HttpMethodSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
  @JsonSerialize(using = HttpMethodSerializer.class)
  @JsonDeserialize(using = HttpMethodDeserializer.class)
  private HttpMethod method;
  private Map<String, RequestHeader> headers;
  private Map<String, RequestUriVariable> uriVariables;
  private Map<String, RequestQueryParam> queryParams;
  private String requestBody;
  private boolean active;
  private RequestType requestType;

  public ApiRequest toApiRequest() {
    return new ApiRequest(
        uri,
        method,
        convertToMultiValueMap(flatEntryValueMap(headers)),
        flatEntryValueMap(uriVariables),
        convertToMultiValueMap(flatEntryValueMap(queryParams)),
        requestBody
    );
  }

}
