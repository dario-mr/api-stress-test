package com.dario.ast.core.service.oauth.provider.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("expires_in")
    Integer expiresIn,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("scope")
    String scope
) {

}