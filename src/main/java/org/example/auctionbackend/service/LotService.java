package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface LotService {
    Page<LotDTO> listByCategory(Long categoryId, int page, int size);

    /**
     * Récupère le détail d’un lot par son id.
     */
    Optional<LotDetailDTO> findById(Long id);
    List<LotDTO> findLatestLots(int count);

    List<LotDTO> getFollowedLots(String username);


}
