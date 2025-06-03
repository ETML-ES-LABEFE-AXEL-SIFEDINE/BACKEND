package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.UserProfileDTO;
import org.example.auctionbackend.model.TransactionType;
import org.example.auctionbackend.model.User;
import org.example.auctionbackend.model.UserTransaction;
import org.example.auctionbackend.repository.UserRepository;
import org.example.auctionbackend.repository.UserTransactionRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final UserTransactionRepository txRepo;

    public UserServiceImpl(UserRepository userRepo,
                           UserTransactionRepository txRepo) {
        this.userRepo = userRepo;
        this.txRepo   = txRepo;
    }

    /**
     * Créditer le compte utilisateur d'un montant valide.
     * Enregistre également la transaction de type TOP_UP.
     *
     * @param username nom d'utilisateur
     * @param amount   montant à créditer
     * @return nouveau solde
     * @throws IllegalArgumentException    si le montant n'est pas < 100
     * @throws UsernameNotFoundException   si l'utilisateur n'existe pas
     */
    @Override
    @Transactional
    public Double topUp(String username, Double amount) {
        // Validation métier
        if (amount == null || amount < 100) {
            throw new IllegalArgumentException("The amount must be ≥ 100 CHF");
        }

        // Récupération de l'utilisateur
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Mise à jour du solde
        double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);
        userRepo.save(user);

        // Historisation de la transaction
        UserTransaction tx = UserTransaction.builder()
                .user(user)
                .amount(amount)
                .type(TransactionType.TOP_UP)
                .timestamp(LocalDateTime.now())
                .build();
        txRepo.save(tx);

        return newBalance;
    }
}
