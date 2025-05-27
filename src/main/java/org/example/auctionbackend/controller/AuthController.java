// src/main/java/org/example/auctionbackend/controller/AuthController.java

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
@Validated
@RequiredArgsConstructor
public class AuthController {

    private static final int MAX_FAILED_ATTEMPTS     = 5;
    private static final long LOCK_TIME_DURATION_MIN = 2; // minutes

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    // ----- CREATE SESSION (login) -----
    @PostMapping("/sessions")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        User user = userOpt.get();

        if (user.isAccountLocked()) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MIN);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body("Account locked until " + unlockTime);
            }
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            user.setFailedAttempts(0);
            userRepository.save(user);

            String accessToken  = jwtUtils.generateAccessToken(auth.getName());
            String refreshToken = jwtUtils.generateRefreshToken(auth.getName());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));

        } catch (BadCredentialsException ex) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // ----- CREATE USER (register) -----
    @PostMapping("/users")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthRequest authRequest) {
        if (userRepository.findByUsername(authRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already in use");
        }
        if (userRepository.findByEmail(authRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User newUser = User.builder()
                .username(authRequest.getUsername())
                .email(authRequest.getEmail())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .balance(0.0)
                .build();
        newUser.getRoles().add("ROLE_USER");
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered");
    }

    // ----- CREATE TOKEN (refresh) -----
    @PostMapping("/tokens")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshRequest req) {
        String refresh = req.getRefreshToken();
        if (!jwtUtils.validateToken(refresh)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username   = jwtUtils.getUsernameFromToken(refresh);
        String newAccess  = jwtUtils.generateAccessToken(username);
        String newRefresh = jwtUtils.generateRefreshToken(username);
        return ResponseEntity.ok(new AuthResponse(newAccess, newRefresh));
    }

    // ----- GET PROFILE -----
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> ResponseEntity.ok(new UserProfileDTO(u.getUsername(), u.getEmail(), u.getBalance())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ----- CHANGE PASSWORD -----
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/users/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        String username = principal.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated");
    }
}
