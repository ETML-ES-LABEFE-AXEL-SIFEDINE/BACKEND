package org.example.auctionbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.BidDTO;
import org.example.auctionbackend.dto.BidRequestDTO;
import org.example.auctionbackend.service.BidService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class BidController {

    private final BidService bidService;

    @PostMapping("/lots/{lotId}/bids")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BidDTO> placeBid(
            @PathVariable Long lotId,
            @RequestBody BidRequestDTO dto,
            Principal principal
    ) {
        BidDTO result = bidService.placeBid(principal.getName(), lotId, dto.getAmount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}

