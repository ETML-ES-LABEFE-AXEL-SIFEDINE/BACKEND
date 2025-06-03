package org.example.auctionbackend.controller;

import org.example.auctionbackend.dto.CategoryDTO;
import org.example.auctionbackend.dto.CategoryTreeDTO;
import org.example.auctionbackend.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    /**
     * GET /api/categories
     * Renvoie les catégories de niveau 1
     */
    @GetMapping
    public List<CategoryDTO> getTopLevelCategories() {
        return service.findTopLevel();
    }

    /**
     * GET /api/categories/{id}/subcategories
     * Renvoie la liste des sous-catégories de la catégorie {id}
     */
    @GetMapping("/{id}/subcategories")
    public List<CategoryDTO> getSubcategories(@PathVariable("id") Long parentId) {
        return service.findSubcategories(parentId);
    }

    @GetMapping("/tree")
    public List<CategoryTreeDTO> getCategoryTree() {
        return service.getCategoryTree();
    }
}
