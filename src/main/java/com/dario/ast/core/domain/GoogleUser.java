package com.dario.ast.core.domain;

public record GoogleUser(
        String name,
        String surname,
        String email,
        String pictureUrl
) {
}
