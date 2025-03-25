package com.dario.ast.core.domain;

public record OAuthUser(
        String name,
        String surname,
        String email,
        String pictureUrl
) {
}
