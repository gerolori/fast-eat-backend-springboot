package com.gerolori.fasteat.web.menu;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.menu.dto.MenuDetailResponse;
import com.gerolori.fasteat.web.menu.dto.MenuAvailabilityStatus;
import com.gerolori.fasteat.web.menu.dto.MenuIngredientResponse;
import com.gerolori.fasteat.web.menu.dto.MenuListItemResponse;
import com.gerolori.fasteat.web.menu.dto.MenuListResponse;
import com.gerolori.fasteat.web.menu.dto.MoneyResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class MenuControllerTest {

    private MenuBrowseService menuBrowseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        menuBrowseService = Mockito.mock(MenuBrowseService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new MenuController(menuBrowseService))
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setValidator(validator)
                .build();
    }

    @Test
    void getMenusReturnsPaginatedCards() throws Exception {
        UUID menuId = UUID.randomUUID();
        MenuListItemResponse item = new MenuListItemResponse(
                menuId,
                "Chicken Bowl",
                "Balanced meal",
                "MAIN",
                new MoneyResponse("8.99", "USD"),
                "https://cdn.example.com/menu.png",
                true,
                MenuAvailabilityStatus.AVAILABLE,
                new java.math.BigDecimal("4.50"),
                20L,
                0.5
        );

        when(menuBrowseService.listMenus(any())).thenReturn(new MenuListResponse(List.of(item), 0, 20, 1, 1, false, false));

        mockMvc.perform(get("/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].menuId").value(menuId.toString()))
                .andExpect(jsonPath("$.items[0].price.amount").value("8.99"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void getMenusBindsFilterQueryParameters() throws Exception {
        when(menuBrowseService.listMenus(any())).thenReturn(new MenuListResponse(List.of(), 1, 5, 0, 0, false, true));

        mockMvc.perform(get("/menus")
                        .param("q", "chicken")
                        .param("category", "MAIN")
                        .param("available", "true")
                        .param("minPrice", "8.00")
                        .param("maxPrice", "12.00")
                        .param("lat", "14.5995")
                        .param("lng", "120.9842")
                        .param("radiusKm", "3")
                        .param("sortBy", "distance")
                        .param("sortDir", "asc")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<MenuBrowseQuery> captor = ArgumentCaptor.forClass(MenuBrowseQuery.class);
        verify(menuBrowseService).listMenus(captor.capture());

        MenuBrowseQuery query = captor.getValue();
        assertThat(query.page()).isEqualTo(1);
        assertThat(query.size()).isEqualTo(5);
        assertThat(query.available()).isTrue();
        assertThat(query.lat()).isEqualTo(14.5995);
        assertThat(query.lng()).isEqualTo(120.9842);
        assertThat(query.radiusKm()).isEqualTo(3.0);
        assertThat(query.categories()).contains("MAIN");
    }

    @Test
    void getMenuReturnsDetail() throws Exception {
        UUID menuId = UUID.randomUUID();
        MenuDetailResponse detail = new MenuDetailResponse(
                menuId,
                "Chicken Bowl",
                "Balanced meal",
                "Detailed description",
                new MoneyResponse("8.99", "USD"),
                true,
                MenuAvailabilityStatus.AVAILABLE,
                "https://cdn.example.com/menu.png",
                List.of(new MenuIngredientResponse(UUID.randomUUID(), "Chicken", "Lean protein", null, true)),
                Instant.parse("2026-04-20T08:30:00Z")
        );

        when(menuBrowseService.getMenu(eq(menuId))).thenReturn(detail);

        mockMvc.perform(get("/menus/{menuId}", menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuId").value(menuId.toString()))
                .andExpect(jsonPath("$.ingredients", hasSize(1)));
    }

    @Test
    void getMenuReturnsNotFoundEnvelopeWhenMissing() throws Exception {
        UUID menuId = UUID.randomUUID();
        when(menuBrowseService.getMenu(eq(menuId))).thenThrow(new ResourceNotFoundException("Menu not found"));

        mockMvc.perform(get("/menus/{menuId}", menuId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/menus/" + menuId))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void getMenuIngredientsReturnsProjectedIngredients() throws Exception {
        UUID menuId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        when(menuBrowseService.getMenuIngredients(menuId)).thenReturn(List.of(
                new MenuIngredientResponse(ingredientId, "Chicken", "Lean protein", "https://cdn.example.com/chicken.png", true)
        ));

        mockMvc.perform(get("/menus/{menuId}/ingredients", menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ingredientId").value(ingredientId.toString()))
                .andExpect(jsonPath("$[0].isAvailable").value(true));
    }
}
