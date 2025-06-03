package org.example.auctionbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.BidDTO;
import org.example.auctionbackend.model.*;
import org.example.auctionbackend.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final UserFollowedLotRepository followedLotRepository;
    private final UserTransactionRepository transactionRepository;

    @Override
    @PreAuthorize("isAuthenticated()")
    public BidDTO placeBid(String username, Long lotId, Double amount) {
        // 1. Charger user & lot
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("No lot"));

        // 2. Vérifications métier
        if (lot.getStatus() != LotStatus.IN_PROGRESS) {
            throw new IllegalStateException("Bids on this lot are not open");
        }
        Double currentPrice = lot.getCurrentPrice() != null
                ? lot.getCurrentPrice()
                : lot.getInitialPrice();
        if (amount <= currentPrice) {
            throw new IllegalArgumentException("The amount must be higher than the list price");
        }
        if (user.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance to place this bid");
        }
        if (lot.getCurrentLeader() != null
                && lot.getCurrentLeader().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You're already the highest bidder");
        }

        // 3. Rembourse l’ancien leader
        User prev = lot.getCurrentLeader();
        if (prev != null) {
            double prevAmt = currentPrice;
            prev.setBalance(prev.getBalance() + prevAmt);
            transactionRepository.save(UserTransaction.builder()
                    .user(prev)
                    .amount(prevAmt)
                    .type(TransactionType.BID_REFUND)
                    .build());
            userRepository.save(prev);
        }

        // 4. Débite le compte du nouvel enchérisseur
        user.setBalance(user.getBalance() - amount);
        transactionRepository.save(UserTransaction.builder()
                .user(user)
                .amount(-amount)
                .type(TransactionType.BID_RESERVE)
                .build());
        userRepository.save(user);

        // 5. Crée la Bid
        Bid bid = Bid.builder()
                .lot(lot)
                .user(user)
                .amount(amount)
                .placedAt(LocalDateTime.now())
                .build();
        bid = bidRepository.save(bid);

        // 6. Met à jour le lot
        lot.setCurrentPrice(amount);
        lot.setCurrentLeader(user);
        lotRepository.save(lot);

        // 7. Suit le lot si pas déjà suivi
        UserFollowedLotId key = new UserFollowedLotId(user.getId(), lot.getId());
        if (!followedLotRepository.existsById(key)) {
            followedLotRepository.save(UserFollowedLot.builder()
                    .id(key)
                    .user(user)
                    .lot(lot)
                    .build());
        }

        // 8. Retourne le DTO
        return BidDTO.builder()
                .id(bid.getId())
                .lotId(lot.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .amount(bid.getAmount())
                .placedAt(Instant.now())
                .build();
    }
}
