package com.dario.ast.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class RunParams {

    private int numRequests;
    private int threadPoolSize;
    private boolean stopOnError;
}
