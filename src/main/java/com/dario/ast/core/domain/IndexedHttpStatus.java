package com.dario.ast.core.domain;

import org.springframework.http.HttpStatus;

public record IndexedHttpStatus(
        int index,
        HttpStatus httpStatus) {
}
