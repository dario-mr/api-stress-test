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
public class EnvVariable implements EntryValue {

  private String value;
  private Instant createdOn;

  public static EnvVariable defaultEnvVariable() {
    return new EnvVariable("", Instant.now());
  }

}
