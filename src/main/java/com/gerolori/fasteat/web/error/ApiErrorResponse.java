package com.gerolori.fasteat.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String error,
        String message,
        int status,
        String path,
        Instant timestamp,
        String traceId,
        Object details
) {
}
