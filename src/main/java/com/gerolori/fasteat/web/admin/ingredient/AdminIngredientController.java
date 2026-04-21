package com.gerolori.fasteat.web.admin.ingredient;

import com.gerolori.fasteat.web.admin.AdminAuthorization;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminIngredientController {

    private final AdminIngredientService adminIngredientService;

    public AdminIngredientController(AdminIngredientService adminIngredientService) {
        this.adminIngredientService = adminIngredientService;
    }

    @PostMapping("/admin/ingredients")
    public ResponseEntity<AdminIngredientResponse> createIngredient(@Valid @RequestBody AdminIngredientUpsertRequest request) {
        AdminAuthorization.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminIngredientService.createIngredient(request));
    }

    @PutMapping("/admin/ingredients/{ingredientId}")
    public AdminIngredientResponse updateIngredient(
            @PathVariable UUID ingredientId,
            @Valid @RequestBody AdminIngredientUpsertRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return adminIngredientService.updateIngredient(ingredientId, request);
    }

    @DeleteMapping("/admin/ingredients/{ingredientId}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable UUID ingredientId) {
        AdminAuthorization.requireAdmin();
        adminIngredientService.deleteIngredient(ingredientId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/menus/{menuId}/ingredients")
    public ResponseEntity<Void> replaceMenuIngredients(
            @PathVariable UUID menuId,
            @Valid @RequestBody AdminMenuIngredientReplaceRequest request
    ) {
        AdminAuthorization.requireAdmin();
        adminIngredientService.replaceMenuIngredients(menuId, request);
        return ResponseEntity.noContent().build();
    }
}
