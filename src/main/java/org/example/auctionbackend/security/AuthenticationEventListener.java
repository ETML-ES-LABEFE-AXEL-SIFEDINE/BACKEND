package org.example.auctionbackend.security;

import org.example.auctionbackend.model.User;
import org.example.auctionbackend.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AuthenticationEventListener {

    private static final int MAX_FAILED_ATTEMPTS     = 5;
    private static final long LOCK_TIME_DURATION_MIN = 15; // verrouillage pendant 15 minutes

    private final UserRepository userRepository;

    public AuthenticationEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sur échec de login : incrémente failedAttempts, et verrouille si seuil atteint.
     */
    @EventListener
    @Transactional
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return;
        }
        User user = opt.get();
        if (user.isAccountLocked()) {
            // déjà verrouillé
            return;
        }

        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    /**
     * Sur succès de login : si le compte était verrouillé et que le délai est écoulé, on débloque ;
     * sinon on reset simplement le compteur d’échecs.
     */
    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return;
        }
        User user = opt.get();

        if (user.isAccountLocked()) {
            // si le délai de verrouillage est écoulé → débloquer
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MIN);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                user.setAccountLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            }
        } else if (user.getFailedAttempts() > 0) {
            // reset du compteur après un login réussi
            user.setFailedAttempts(0);
            userRepository.save(user);
        }
    }
}
