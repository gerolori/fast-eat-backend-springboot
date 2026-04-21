package com.gerolori.fasteat.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TraceIdResolver {

    private static final String REQUEST_CORRELATION_ATTRIBUTE = "fasteat.request.correlationId";

    public String resolve(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_CORRELATION_ATTRIBUTE);
        if (value != null && StringUtils.hasText(value.toString())) {
            return value.toString();
        }

        String requestId = request.getRequestId();
        if (StringUtils.hasText(requestId)) {
            return requestId;
        }

        return UUID.randomUUID().toString();
    }
}
