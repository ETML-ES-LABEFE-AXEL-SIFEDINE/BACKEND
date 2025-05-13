package org.example.auctionbackend.controller;

import org.example.auctionbackend.dto.TransactionDTO;
import org.example.auctionbackend.dto.TopUpRequestDTO;
import org.example.auctionbackend.dto.UserProfileDTO;
import org.example.auctionbackend.model.User;
import org.example.auctionbackend.repository.UserRepository;
import org.example.auctionbackend.service.TransactionService;
import org.example.auctionbackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          TransactionService transactionService) {
        this.userService        = userService;
        this.userRepository     = userRepository;
        this.transactionService = transactionService;
    }

    /**
     * Approvisionner le compte de l'utilisateur authentifié.
     */
    @PostMapping("/top-up")
    public ResponseEntity<UserProfileDTO> topUp(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody TopUpRequestDTO req) {

        // Créditer le solde via le service
        userService.topUp(ud.getUsername(), req.getAmount());

        // Recharger l'utilisateur pour récupérer email et solde exact
        Optional<User> userOpt = userRepository.findByUsername(ud.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOpt.get();

        // Construire le DTO avec toutes les infos
        UserProfileDTO dto = new UserProfileDTO(
                user.getUsername(),
                user.getEmail(),
                user.getBalance()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Récupérer l'historique des approvisionnements de l'utilisateur.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(
            @AuthenticationPrincipal UserDetails ud) {

        List<TransactionDTO> history = transactionService.getHistory(ud.getUsername());
        return ResponseEntity.ok(history);
    }
}
