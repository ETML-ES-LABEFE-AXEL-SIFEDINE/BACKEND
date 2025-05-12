package org.example.auctionbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Catégorie avec ses sous-catégories.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTreeDTO {
    private Long id;
    private String name;
    private List<CategoryDTO> subcategories;
}
