package org.example.auctionbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LotDTO {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private Double initialPrice;
    private Double currentBid;  // enchère courante
}
