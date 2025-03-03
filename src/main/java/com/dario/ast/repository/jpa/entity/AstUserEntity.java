package com.dario.ast.repository.jpa.entity;

import static jakarta.persistence.CascadeType.ALL;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.IDENTITY;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ast_user", schema = "my_schema")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AstUserEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "created_on", nullable = false)
  private Instant createdOn;

  @Column(name = "active", nullable = false)
  private boolean active;

  @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<AstRequestEntity> requests = new ArrayList<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AstUserEntity astUser = (AstUserEntity) o;
    return id != null && id.equals(astUser.id);
  }

  @Override
  public int hashCode() {
    return id == null ? 0 : id.hashCode();
  }
}
