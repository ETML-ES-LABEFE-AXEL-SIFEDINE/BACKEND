package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);
}
