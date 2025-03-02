package com.dario.ast.core.domain;

import lombok.*;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "requestId")
public final class ConfigParams {

    private Long requestId;
    private String requestName;
    private User user;

    private String uri;
    private HttpMethod method;
    private Map<String, String> headers;
    private Map<String, String> uriVariables;
    private Map<String, String> queryParams;
    private String requestBody;
}
