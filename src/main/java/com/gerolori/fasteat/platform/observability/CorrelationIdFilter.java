package com.gerolori.fasteat.platform.observability;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = RequestCorrelation.resolveOrGenerate(request.getHeader(RequestCorrelation.HEADER_NAME));
        String previousCorrelationId = MDC.get(RequestCorrelation.MDC_CORRELATION_ID_KEY);
        String previousTraceId = MDC.get(RequestCorrelation.MDC_TRACE_ID_KEY);

        request.setAttribute(RequestCorrelation.REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(RequestCorrelation.HEADER_NAME, correlationId);
        MDC.put(RequestCorrelation.MDC_CORRELATION_ID_KEY, correlationId);
        MDC.put(RequestCorrelation.MDC_TRACE_ID_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            restoreMdc(RequestCorrelation.MDC_CORRELATION_ID_KEY, previousCorrelationId);
            restoreMdc(RequestCorrelation.MDC_TRACE_ID_KEY, previousTraceId);
        }
    }

    private static void restoreMdc(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
            return;
        }

        MDC.put(key, previousValue);
    }
}
