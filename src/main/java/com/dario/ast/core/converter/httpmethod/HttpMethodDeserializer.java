package com.dario.ast.core.converter.httpmethod;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.http.HttpMethod;

import java.io.IOException;

public class HttpMethodDeserializer extends JsonDeserializer<HttpMethod> {

    @Override
    public HttpMethod deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var method = p.getText().toUpperCase();
        return HttpMethod.valueOf(method);
    }
}
