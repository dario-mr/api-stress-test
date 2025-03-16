package com.dario.ast.repository.jpa.entity;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ast_environment", schema = "my_schema")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AstEnvironmentEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "created_on", nullable = false)
  private Instant createdOn;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ElementCollection(fetch = EAGER)
  @MapKeyColumn(name = "variable_key")
  @Column(name = "variable_value")
  @CollectionTable(
      name = "ast_env_variables",
      schema = "my_schema",
      joinColumns = @JoinColumn(name = "ast_environment_id"))
  @OrderBy("created_on ASC")
  private Map<String, AstEnvVariableEntity> variables;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    AstEnvironmentEntity otherEnv = (AstEnvironmentEntity) other;
    return id != null && id.equals(otherEnv.id);
  }

  @Override
  public int hashCode() {
    return id == null ? 0 : id.hashCode();
  }
}
