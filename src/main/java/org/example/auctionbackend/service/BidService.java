package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.BidDTO;

public interface BidService {
    /**
     * Place une enchère pour l'utilisateur identifié.
     * @param username le nom d'utilisateur
     * @param lotId     l'id du lot
     * @param amount    le montant de l'enchère
     * @return DTO de l'enchère créée
     */
    BidDTO placeBid(String username, Long lotId, Double amount);
}
