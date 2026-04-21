package com.gerolori.fasteat.web.admin.media;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AdminMediaControllerTest {

    private AdminMediaService adminMediaService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminMediaService = Mockito.mock(AdminMediaService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminMediaController(adminMediaService))
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setValidator(validator)
                .build();

        authenticateAs("ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateRestaurantImageReturnsUpdatedPayload() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(adminMediaService.updateRestaurantImage(eq(restaurantId), eq("https://cdn.example.com/rest.jpg")))
                .thenReturn(new AdminImageUpdateResponse(restaurantId, "https://cdn.example.com/rest.jpg", Instant.now()));

        mockMvc.perform(put("/admin/media/restaurants/{restaurantId}/image", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"imageUrl\":\"https://cdn.example.com/rest.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value(restaurantId.toString()));
    }

    @Test
    void updateMenuImageMapsMissingResource() throws Exception {
        UUID menuId = UUID.randomUUID();
        when(adminMediaService.updateMenuImage(eq(menuId), eq("https://cdn.example.com/menu.jpg")))
                .thenThrow(new ResourceNotFoundException("Menu not found"));

        mockMvc.perform(put("/admin/media/menus/{menuId}/image", menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"imageUrl\":\"https://cdn.example.com/menu.jpg\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void rejectsNonAdminRole() throws Exception {
        UUID menuId = UUID.randomUUID();
        authenticateAs("ROLE_CUSTOMER");

        mockMvc.perform(put("/admin/media/menus/{menuId}/image", menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"imageUrl\":\"https://cdn.example.com/menu.jpg\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));

        verifyNoInteractions(adminMediaService);
    }

    private void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID(),
                "n/a",
                List.of(new SimpleGrantedAuthority(authority))
        ));
    }
}
