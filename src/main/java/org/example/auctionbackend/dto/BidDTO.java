package org.example.auctionbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Représentation d’une enchère sur un lot (placeholder pour future implémentation).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidDTO {
    private Long id;
    private String bidderUsername;
    private Double amount;
    private LocalDateTime timestamp;
}
