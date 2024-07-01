package com.dario.ast.core.converter.httpmethod;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.HttpMethod;

import java.io.IOException;

public class HttpMethodSerializer extends JsonSerializer<HttpMethod> {

    @Override
    public void serialize(HttpMethod httpMethod, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeString(httpMethod.name());
    }
}
