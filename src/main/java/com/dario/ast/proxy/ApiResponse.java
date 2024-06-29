package com.dario.ast.proxy;

import org.springframework.http.HttpStatus;

public record ApiResponse(
        HttpStatus statusCode,
        String errorMessage
) {
}
