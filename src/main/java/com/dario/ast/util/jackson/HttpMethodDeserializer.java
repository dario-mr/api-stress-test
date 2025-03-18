package com.dario.ast.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.springframework.http.HttpMethod;

public class HttpMethodDeserializer extends JsonDeserializer<HttpMethod> {

  @Override
  public HttpMethod deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return HttpMethod.valueOf(p.getText());
  }
}
