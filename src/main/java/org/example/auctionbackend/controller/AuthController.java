package org.example.auctionbackend.controller;

import org.example.auctionbackend.auth.AuthRequest;
import org.example.auctionbackend.auth.AuthResponse;
import org.example.auctionbackend.auth.RefreshRequest;
import org.example.auctionbackend.auth.LoginRequest;
import org.example.auctionbackend.auth.ChangePasswordRequest;
import org.example.auctionbackend.dto.UserProfileDTO;
import org.example.auctionbackend.model.User;
import org.example.auctionbackend.repository.UserRepository;
import org.example.auctionbackend.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;


import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private static final int MAX_FAILED_ATTEMPTS     = 5;
    private static final long LOCK_TIME_DURATION_MIN = 2; // minutes

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    // ----- LOGIN -----
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiants invalides.");
        }
        User user = userOpt.get();

        if (user.isAccountLocked()) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MIN);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body("Compte verrouillé jusqu’à " + unlockTime);
            }
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            user.setFailedAttempts(0);
            userRepository.save(user);

            String accessToken  = jwtUtils.generateAccessToken(authentication.getName());
            String refreshToken = jwtUtils.generateRefreshToken(authentication.getName());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));

        } catch (BadCredentialsException e) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiants invalides.");
        }
    }

    // ----- REGISTER -----
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        if (userRepository.findByUsername(authRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Nom d'utilisateur déjà utilisé");
        }
        if (userRepository.findByEmail(authRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email déjà utilisé");
        }

        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());
        User newUser = User.builder()
                .username(authRequest.getUsername())
                .email(authRequest.getEmail())
                .password(encodedPassword)
                .balance(0.0)
                .build();
        newUser.getRoles().add("ROLE_USER");
        userRepository.save(newUser);

        return ResponseEntity.ok("Utilisateur enregistré avec succès");
    }

    // ----- REFRESH -----
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshRequest req
    ) {
        String refresh = req.getRefreshToken();
        if (!jwtUtils.validateToken(refresh)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username   = jwtUtils.getUsernameFromToken(refresh);
        String newAccess  = jwtUtils.generateAccessToken(username);
        String newRefresh = jwtUtils.generateRefreshToken(username);
        return ResponseEntity.ok(new AuthResponse(newAccess, newRefresh));
    }

    // ----- PROFILE -----
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/me")
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .map(u -> ResponseEntity.ok(
                        new UserProfileDTO(u.getUsername(), u.getEmail(), u.getBalance())
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ----- CHANGE PASSWORD -----
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        String username = userDetails.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
        }
        User user = userOpt.get();

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ancien mot de passe incorrect.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }
}
