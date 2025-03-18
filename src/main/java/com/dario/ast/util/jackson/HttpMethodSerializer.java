package com.dario.ast.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.springframework.http.HttpMethod;

public class HttpMethodSerializer extends JsonSerializer<HttpMethod> {

  @Override
  public void serialize(HttpMethod value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.name());
  }

}
