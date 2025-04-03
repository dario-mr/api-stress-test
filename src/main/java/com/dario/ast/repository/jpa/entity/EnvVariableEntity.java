package com.dario.ast.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EnvVariableEntity {

  @Column(name = "variable_value", columnDefinition = "TEXT")
  private String value;

  @Column(name = "created_on")
  private Instant createdOn;

}
