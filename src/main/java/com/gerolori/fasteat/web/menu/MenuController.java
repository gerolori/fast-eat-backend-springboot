package com.gerolori.fasteat.web.menu;

import com.gerolori.fasteat.web.menu.dto.MenuDetailResponse;
import com.gerolori.fasteat.web.menu.dto.MenuIngredientResponse;
import com.gerolori.fasteat.web.menu.dto.MenuListResponse;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/menus")
public class MenuController {

    private final MenuBrowseService menuBrowseService;

    public MenuController(MenuBrowseService menuBrowseService) {
        this.menuBrowseService = menuBrowseService;
    }

    @GetMapping
    public MenuListResponse browseMenus(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        MenuBrowseQuery query = new MenuBrowseQuery(
                q,
                category,
                available,
                minPrice,
                maxPrice,
                lat,
                lng,
                radiusKm,
                sortBy,
                sortDir,
                page,
                size
        );

        return menuBrowseService.listMenus(query);
    }

    @GetMapping("/{menuId}")
    public MenuDetailResponse getMenuById(@PathVariable UUID menuId) {
        return menuBrowseService.getMenu(menuId);
    }

    @GetMapping("/{menuId}/ingredients")
    public List<MenuIngredientResponse> getMenuIngredients(@PathVariable UUID menuId) {
        return menuBrowseService.getMenuIngredients(menuId);
    }
}
