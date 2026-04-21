package com.gerolori.fasteat.platform.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void usesIncomingCorrelationIdForRequestResponseAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        request.addHeader(RequestCorrelation.HEADER_NAME, " incoming-id ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> correlationIdInChain = new AtomicReference<>();
        AtomicReference<String> traceIdInChain = new AtomicReference<>();
        AtomicReference<String> requestAttributeInChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            correlationIdInChain.set(MDC.get(RequestCorrelation.MDC_CORRELATION_ID_KEY));
            traceIdInChain.set(MDC.get(RequestCorrelation.MDC_TRACE_ID_KEY));
            requestAttributeInChain
                    .set((String) request.getAttribute(RequestCorrelation.REQUEST_ATTRIBUTE));
        });

        assertEquals("incoming-id", correlationIdInChain.get());
        assertEquals("incoming-id", traceIdInChain.get());
        assertEquals("incoming-id", requestAttributeInChain.get());
        assertEquals("incoming-id", response.getHeader(RequestCorrelation.HEADER_NAME));
        assertNull(MDC.get(RequestCorrelation.MDC_CORRELATION_ID_KEY));
        assertNull(MDC.get(RequestCorrelation.MDC_TRACE_ID_KEY));
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> correlationIdInChain = new AtomicReference<>();
        AtomicReference<String> traceIdInChain = new AtomicReference<>();
        AtomicReference<String> requestAttributeInChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            correlationIdInChain.set(MDC.get(RequestCorrelation.MDC_CORRELATION_ID_KEY));
            traceIdInChain.set(MDC.get(RequestCorrelation.MDC_TRACE_ID_KEY));
            requestAttributeInChain
                    .set((String) request.getAttribute(RequestCorrelation.REQUEST_ATTRIBUTE));
        });

        String generatedCorrelationId = response.getHeader(RequestCorrelation.HEADER_NAME);
        assertNotNull(generatedCorrelationId);
        UUID.fromString(generatedCorrelationId);

        assertEquals(generatedCorrelationId, correlationIdInChain.get());
        assertEquals(generatedCorrelationId, traceIdInChain.get());
        assertEquals(generatedCorrelationId, requestAttributeInChain.get());
        assertNull(MDC.get(RequestCorrelation.MDC_CORRELATION_ID_KEY));
        assertNull(MDC.get(RequestCorrelation.MDC_TRACE_ID_KEY));
    }
}
