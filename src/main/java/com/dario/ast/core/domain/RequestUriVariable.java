package com.dario.ast.core.domain;

import com.dario.ast.view.component.common.entries.EntryValue;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUriVariable implements EntryValue {

  private String value;
  private Instant createdOn;

  public static RequestUriVariable defaultRequestUriVariable() {
    return new RequestUriVariable("", Instant.now());
  }

}
