package org.example.auctionbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BidDTO {
    private Long id;
    private Long lotId;
    private Long userId;
    private String username;
    private Double amount;
    private Instant placedAt;
}
