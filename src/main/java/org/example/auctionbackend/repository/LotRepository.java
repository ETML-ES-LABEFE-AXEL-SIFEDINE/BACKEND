package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.Lot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LotRepository extends JpaRepository<Lot, Long> {
    Page<Lot> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);
    List<Lot> findAllByOrderByIdDesc(Pageable pageable);
}
