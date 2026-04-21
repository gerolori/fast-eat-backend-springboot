package com.gerolori.fasteat.platform.operational;

import com.gerolori.fasteat.platform.observability.CorrelationIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OperationalStatusControllerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OperationalStatusController())
                .addFilter(new CorrelationIdFilter())
                .build();
    }

    @Test
    void healthEndpointReturnsMinimalAliveStatus() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(header().string("X-Correlation-Id", not(blankOrNullString())));
    }

    @Test
    void readinessEndpointReturnsDistinctMinimalTrafficStatus() throws Exception {
        mockMvc.perform(get("/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(header().string("X-Correlation-Id", not(blankOrNullString())));
    }
}
