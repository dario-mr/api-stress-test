package com.dario.ast.core.domain;

import java.time.Instant;

public record OAuthToken(
    String userId,
    String refreshToken,
    Instant expiresAt,
    Instant lastUpdated,
    String provider
) {

}
