package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.CategoryDTO;
import org.example.auctionbackend.dto.CategoryTreeDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> findTopLevel();
    List<CategoryDTO> findSubcategories(Long parentId);

    List<CategoryTreeDTO> getCategoryTree();

}
