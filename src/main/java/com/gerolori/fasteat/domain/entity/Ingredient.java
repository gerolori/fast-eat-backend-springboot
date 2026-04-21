package com.gerolori.fasteat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "summary", length = 255)
    private String summary;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "category", length = 80)
    private String category;

    @Column(name = "available", nullable = false)
    private boolean available = true;

    @Column(name = "image_url", length = 512)
    private String imageUrl;
}
