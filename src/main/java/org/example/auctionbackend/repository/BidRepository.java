package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
