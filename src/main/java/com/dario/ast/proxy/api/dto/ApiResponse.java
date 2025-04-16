package com.dario.ast.proxy.api.dto;

import org.springframework.http.HttpStatus;

public record ApiResponse(
    HttpStatus statusCode,
    String errorMessage,
    String responseBody
) {

}
