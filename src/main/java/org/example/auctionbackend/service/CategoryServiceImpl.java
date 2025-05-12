package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.CategoryDTO;
import org.example.auctionbackend.dto.CategoryTreeDTO;
import org.example.auctionbackend.model.Category;
import org.example.auctionbackend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<CategoryDTO> findTopLevel() {
        return repo.findByParentIsNull().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> parents = repo.findByParentIsNull();
        return parents.stream().map(parent -> {
            List<CategoryDTO> children = repo.findByParentId(parent.getId())
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return new CategoryTreeDTO(parent.getId(), parent.getName(), children);
        }).collect(Collectors.toList());
    }


    @Override
    public List<CategoryDTO> findSubcategories(Long parentId) {
        return repo.findByParentId(parentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO toDTO(Category c) {
        return new CategoryDTO(c.getId(), c.getName());
    }
}
