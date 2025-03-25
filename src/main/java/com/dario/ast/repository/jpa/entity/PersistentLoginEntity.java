package com.dario.ast.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "persistent_logins", schema = "my_schema")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PersistentLoginEntity {

  @Id
  @Column(name = "series", length = 64)
  private String series;

  @Column(name = "username", length = 64, nullable = false)
  private String username;

  @Column(name = "token", length = 64, nullable = false)
  private String token;

  @Column(name = "last_used", nullable = false)
  private LocalDateTime lastUsed;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    PersistentLoginEntity otherEntity = (PersistentLoginEntity) other;
    return series != null && series.equals(otherEntity.series);
  }

  @Override
  public int hashCode() {
    return series == null ? 0 : series.hashCode();
  }

}
