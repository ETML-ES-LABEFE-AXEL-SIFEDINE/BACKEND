package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * Supprime toutes les enchères (bids) associées à un lot donné.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Bid b WHERE b.lot.id = :lotId")
    void deleteAllByLotId(Long lotId);
}
