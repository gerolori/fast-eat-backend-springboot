package com.gerolori.fasteat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final TraceIdResolver traceIdResolver;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper, TraceIdResolver traceIdResolver) {
        this.objectMapper = objectMapper;
        this.traceIdResolver = traceIdResolver;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse payload = new ApiErrorResponse(
                "AUTHZ_INSUFFICIENT_ROLE",
                accessDeniedException.getMessage() == null || accessDeniedException.getMessage().isBlank()
                        ? "Access denied"
                        : accessDeniedException.getMessage(),
                HttpServletResponse.SC_FORBIDDEN,
                request.getRequestURI(),
                Instant.now(),
                traceIdResolver.resolve(request),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), payload);
    }
}
