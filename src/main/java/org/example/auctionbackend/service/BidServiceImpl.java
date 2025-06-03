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
    private final LotServiceImpl lotService; // pour rafraîchir le status

    @Override
    @PreAuthorize("isAuthenticated()")
    public BidDTO placeBid(String username, Long lotId, Double amount) {
        // 1. Charger user & lot
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("No lot"));

        // 1.1 Mettre à jour le statut du lot en fonction des dates
        lotService.refreshLotStatus(lot);
        // recharger le lot après mise à jour éventuelle du status
        lot = lotRepository.findById(lotId).orElseThrow();

        // 2. Vérifications métier sur le statut
        if (lot.getStatus() != LotStatus.IN_PROGRESS) {
            throw new IllegalStateException("Bids on this lot are not open");
        }

        // 3. Vérifier que le montant est supérieur au prix courant
        Double currentPrice = (lot.getCurrentPrice() != null)
                ? lot.getCurrentPrice()
                : lot.getInitialPrice();
        if (amount <= currentPrice) {
            throw new IllegalArgumentException("The amount must be higher than the current price");
        }

        // 4. Vérifier solde utilisateur
        if (user.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance to place this bid");
        }

        // 5. Vérifier que l'utilisateur n'est pas déjà le meilleur enchérisseur
        if (lot.getCurrentLeader() != null
                && lot.getCurrentLeader().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You're already the highest bidder");
        }

        // 6. Rembourser l’ancien leader si nécessaire
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

        // 7. Débiter le compte du nouvel enchérisseur
        user.setBalance(user.getBalance() - amount);
        transactionRepository.save(UserTransaction.builder()
                .user(user)
                .amount(-amount)
                .type(TransactionType.BID_RESERVE)
                .build());
        userRepository.save(user);

        // 8. Créer la nouvelle Bid
        Bid bid = Bid.builder()
                .lot(lot)
                .user(user)
                .amount(amount)
                .placedAt(LocalDateTime.now())
                .build();
        bid = bidRepository.save(bid);

        // 9. Mettre à jour le lot (currentPrice + currentLeader)
        lot.setCurrentPrice(amount);
        lot.setCurrentLeader(user);
        lotRepository.save(lot);

        // 10. Suivre le lot si pas déjà suivi
        UserFollowedLotId key = new UserFollowedLotId(user.getId(), lot.getId());
        if (!followedLotRepository.existsById(key)) {
            followedLotRepository.save(UserFollowedLot.builder()
                    .id(key)
                    .user(user)
                    .lot(lot)
                    .build());
        }

        // 11. Retourner le DTO de la nouvelle enchère
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
