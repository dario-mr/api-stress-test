package com.dario.ast.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "oauth_token", schema = "my_schema")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OAuthTokenEntity {

  @Id
  @Column(name = "user_id")
  private String userId; // e.g. google sub

  @Column(name = "refresh_token")
  private String refreshToken; // encrypted

  @Column(name = "provider")
  private String provider; // e.g. "google", "github", "apple"

  @Column(name = "expires_at")
  private Instant expiresAt; // access token expiration (not refresh token!)

  @Column(name = "last_updated")
  private Instant lastUpdated;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuthTokenEntity otherEntity = (OAuthTokenEntity) o;
    return userId != null && userId.equals(otherEntity.userId);
  }

  @Override
  public int hashCode() {
    return userId == null ? 0 : userId.hashCode();
  }

}
