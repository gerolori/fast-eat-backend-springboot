package com.gerolori.fasteat.web.admin.ingredient;

import com.gerolori.fasteat.domain.entity.Ingredient;
import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.repository.IngredientRepository;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminIngredientService {

    private final IngredientRepository ingredientRepository;
    private final MenuRepository menuRepository;

    public AdminIngredientService(IngredientRepository ingredientRepository, MenuRepository menuRepository) {
        this.ingredientRepository = ingredientRepository;
        this.menuRepository = menuRepository;
    }

    public AdminIngredientResponse createIngredient(AdminIngredientUpsertRequest request) {
        Ingredient ingredient = new Ingredient();
        applyUpsert(ingredient, request);
        return toResponse(ingredientRepository.save(ingredient));
    }

    public AdminIngredientResponse updateIngredient(UUID ingredientId, AdminIngredientUpsertRequest request) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + ingredientId));

        applyUpsert(ingredient, request);
        return toResponse(ingredient);
    }

    public void deleteIngredient(UUID ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + ingredientId));
        ingredientRepository.delete(ingredient);
    }

    public void replaceMenuIngredients(UUID menuId, AdminMenuIngredientReplaceRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));

        Set<UUID> requestedIds = new LinkedHashSet<>(request.ingredientIds());
        Set<Ingredient> ingredients = new LinkedHashSet<>(ingredientRepository.findAllById(requestedIds));
        if (ingredients.size() != requestedIds.size()) {
            throw new ResourceNotFoundException("One or more ingredients were not found");
        }

        menu.getIngredients().clear();
        menu.getIngredients().addAll(ingredients);
    }

    private void applyUpsert(Ingredient ingredient, AdminIngredientUpsertRequest request) {
        ingredient.setName(request.name().trim());
        ingredient.setSummary(request.summary());
        ingredient.setDescription(request.description());
        ingredient.setCategory(request.category());
        ingredient.setImageUrl(request.imageUrl());
        ingredient.setAvailable(request.available() == null || request.available());
    }

    private AdminIngredientResponse toResponse(Ingredient ingredient) {
        return new AdminIngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getSummary(),
                ingredient.getDescription(),
                ingredient.getCategory(),
                ingredient.isAvailable(),
                ingredient.getImageUrl(),
                ingredient.getUpdatedAt()
        );
    }
}
