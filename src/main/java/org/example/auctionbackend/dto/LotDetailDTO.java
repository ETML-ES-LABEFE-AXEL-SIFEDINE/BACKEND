package org.example.auctionbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Détail d’un lot, avec ses principales propriétés et (éventuellement) la liste des enchères.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LotDetailDTO {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private Double initialPrice;
    private Double currentBid;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    // Pour l'instant, on met une liste vide ou stub ; plus tard listera les bids
    private List<BidDTO> bids;
}
