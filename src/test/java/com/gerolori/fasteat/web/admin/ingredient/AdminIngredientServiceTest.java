package com.gerolori.fasteat.web.admin.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Ingredient;
import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.repository.IngredientRepository;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminIngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private MenuRepository menuRepository;

    private AdminIngredientService service;

    @BeforeEach
    void setUp() {
        service = new AdminIngredientService(ingredientRepository, menuRepository);
    }

    @Test
    void createIngredientDefaultsAvailabilityToTrue() {
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0, Ingredient.class));

        var response = service.createIngredient(new AdminIngredientUpsertRequest("Cheese", null, null, null, null, null));

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    void replaceMenuIngredientsOverwritesComposition() {
        UUID menuId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();

        Menu menu = new Menu();
        Ingredient existing = new Ingredient();
        existing.setId(UUID.randomUUID());
        menu.getIngredients().add(existing);

        Ingredient replacement = new Ingredient();
        replacement.setId(ingredientId);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(ingredientRepository.findAllById(Set.of(ingredientId))).thenReturn(List.of(replacement));

        service.replaceMenuIngredients(menuId, new AdminMenuIngredientReplaceRequest(List.of(ingredientId)));

        assertThat(menu.getIngredients()).containsExactly(replacement);
    }

    @Test
    void replaceMenuIngredientsFailsWhenAnyIngredientIsMissing() {
        UUID menuId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Menu menu = new Menu();

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(ingredientRepository.findAllById(Set.of(ingredientId))).thenReturn(List.of());

        assertThatThrownBy(() -> service.replaceMenuIngredients(menuId, new AdminMenuIngredientReplaceRequest(List.of(ingredientId))))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
