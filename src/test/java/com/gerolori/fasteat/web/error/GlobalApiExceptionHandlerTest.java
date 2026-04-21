package com.gerolori.fasteat.web.error;

import com.gerolori.fasteat.config.TraceIdResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setValidator(validator)
                .build();
    }

    @Test
    void mapsValidationErrorsToStableEnvelope() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())))
                .andExpect(jsonPath("$.details.length()", greaterThan(0)));
    }

    @Test
    void mapsAuthenticationErrorsToAuthCode() throws Exception {
        mockMvc.perform(get("/test/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_TOKEN"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void mapsNotFoundErrorsToStableEnvelope() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void mapsBusinessRuleErrorsToStableEnvelope() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void usesRequestCorrelationAttributeAsTraceIdWhenPresent() throws Exception {
        mockMvc.perform(get("/test/not-found")
                        .requestAttr("fasteat.request.correlationId", "corr-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.traceId").value("corr-123"));
    }

    @Test
    void mapsAuthorizationErrorsToAuthzCode() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        void validation(@Valid @RequestBody ValidationRequest request) {
        }

        @GetMapping("/auth")
        void auth() {
            throw new BadCredentialsException("Bad token");
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new AccessDeniedException("Insufficient role");
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Order not found");
        }

        @GetMapping("/business")
        void businessRule() {
            throw new BusinessRuleViolationException("Cannot transition order state");
        }
    }

    record ValidationRequest(@NotBlank String requiredField) {
    }
}
