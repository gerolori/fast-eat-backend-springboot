package com.gerolori.fasteat.web.admin.ingredient;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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

class AdminIngredientControllerTest {

    private AdminIngredientService adminIngredientService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        adminIngredientService = Mockito.mock(AdminIngredientService.class);
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminIngredientController(adminIngredientService))
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
    void createIngredientReturnsCreated() throws Exception {
        UUID ingredientId = UUID.randomUUID();
        when(adminIngredientService.createIngredient(Mockito.any())).thenReturn(new AdminIngredientResponse(
                ingredientId,
                "Chicken",
                "Lean protein",
                null,
                "PROTEIN",
                true,
                "https://cdn.example.com/ingredients/chicken.png",
                Instant.parse("2026-04-21T10:00:00Z")
        ));

        mockMvc.perform(post("/admin/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Chicken",
                                  "summary": "Lean protein"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ingredientId").value(ingredientId.toString()));
    }

    @Test
    void replaceMenuIngredientsReturnsNoContent() throws Exception {
        UUID menuId = UUID.randomUUID();

        mockMvc.perform(put("/admin/menus/{menuId}/ingredients", menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminMenuIngredientReplaceRequest(java.util.List.of(UUID.randomUUID())))))
                .andExpect(status().isNoContent());

        verify(adminIngredientService).replaceMenuIngredients(eq(menuId), Mockito.any());
    }

    @Test
    void deleteIngredientMapsNotFoundToEnvelope() throws Exception {
        UUID ingredientId = UUID.randomUUID();
        Mockito.doThrow(new ResourceNotFoundException("Ingredient not found"))
                .when(adminIngredientService)
                .deleteIngredient(ingredientId);

        mockMvc.perform(delete("/admin/ingredients/{ingredientId}", ingredientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void replaceIngredientsRejectsNonAdminRole() throws Exception {
        UUID menuId = UUID.randomUUID();
        authenticateAs("ROLE_CUSTOMER");

        mockMvc.perform(put("/admin/menus/{menuId}/ingredients", menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminMenuIngredientReplaceRequest(java.util.List.of(UUID.randomUUID())))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));

        verifyNoInteractions(adminIngredientService);
    }

    private void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID(),
                "n/a",
                List.of(new SimpleGrantedAuthority(authority))
        ));
    }
}
