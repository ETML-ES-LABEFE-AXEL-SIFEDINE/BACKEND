package org.example.auctionbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<BidDTO> bids;
    private String currentLeaderUsername;
    private String ownerUsername;
}
