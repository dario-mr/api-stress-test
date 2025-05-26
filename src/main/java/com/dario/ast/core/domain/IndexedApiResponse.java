package com.dario.ast.core.domain;

import com.dario.ast.proxy.api.dto.ApiResponse;

public record IndexedApiResponse(
    int index,
    ApiResponse response
) {

}
