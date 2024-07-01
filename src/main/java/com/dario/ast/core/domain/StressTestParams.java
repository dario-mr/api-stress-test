package com.dario.ast.core.domain;

import com.dario.ast.core.converter.httpmethod.HttpMethodDeserializer;
import com.dario.ast.core.converter.httpmethod.HttpMethodSerializer;
import com.dario.ast.proxy.ApiRequest;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static com.dario.ast.util.MapUtil.convertToMultiValueMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class StressTestParams {

    private int numRequests;
    private int threadPoolSize;
    private String uri;

    @JsonSerialize(using = HttpMethodSerializer.class)
    @JsonDeserialize(using = HttpMethodDeserializer.class)
    private HttpMethod method;

    private Map<String, String> headers;
    private Map<String, String> uriVariables;
    private Map<String, String> queryParams;
    private String requestBody;
    private boolean stopOnError;

    public ApiRequest toApiRequest() {
        return new ApiRequest(
                uri,
                method,
                convertToMultiValueMap(headers),
                uriVariables,
                convertToMultiValueMap(queryParams),
                requestBody
        );
    }
}
