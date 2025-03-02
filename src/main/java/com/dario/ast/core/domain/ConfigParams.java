package com.dario.ast.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigParams that = (ConfigParams) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }
}
