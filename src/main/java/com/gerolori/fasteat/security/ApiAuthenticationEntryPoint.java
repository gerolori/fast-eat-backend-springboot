package com.gerolori.fasteat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final TraceIdResolver traceIdResolver;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper, TraceIdResolver traceIdResolver) {
        this.objectMapper = objectMapper;
        this.traceIdResolver = traceIdResolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String errorCode = resolveErrorCode(request, authenticationException);
        String message = authenticationException != null && authenticationException.getMessage() != null
                ? authenticationException.getMessage()
                : "Authentication failed";

        ApiErrorResponse payload = new ApiErrorResponse(
                errorCode,
                message,
                HttpServletResponse.SC_UNAUTHORIZED,
                request.getRequestURI(),
                Instant.now(),
                traceIdResolver.resolve(request),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), payload);
    }

    private String resolveErrorCode(HttpServletRequest request, AuthenticationException exception) {
        if (exception instanceof JwtAuthenticationException jwtAuthenticationException) {
            return jwtAuthenticationException.getErrorCode();
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return "AUTH_MISSING_TOKEN";
        }

        return "AUTH_INVALID_TOKEN";
    }
}
