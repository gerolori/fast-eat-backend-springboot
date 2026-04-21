package com.gerolori.fasteat.domain.repository;

import com.gerolori.fasteat.domain.entity.Ingredient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    List<Ingredient> findByAvailableTrue();
}
