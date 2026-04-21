package com.gerolori.fasteat.platform.observability;

import java.util.UUID;

import org.springframework.util.StringUtils;

public final class RequestCorrelation {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String REQUEST_ATTRIBUTE = "fasteat.request.correlationId";
    public static final String MDC_CORRELATION_ID_KEY = "correlationId";
    public static final String MDC_TRACE_ID_KEY = "traceId";

    private RequestCorrelation() {
    }

    public static String resolveOrGenerate(String headerValue) {
        if (StringUtils.hasText(headerValue)) {
            return headerValue.trim();
        }

        return UUID.randomUUID().toString();
    }
}
