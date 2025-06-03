package org.example.auctionbackend.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.model.Lot;
import org.example.auctionbackend.model.LotStatus;
import org.example.auctionbackend.repository.LotRepository;
import org.example.auctionbackend.service.LotServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler qui s'exécute toutes les minutes (ou selon cron choisi)
 * pour détecter les lots IN_PROGRESS dont endDate est dépassée,
 * puis appeler refreshLotStatus(...) dessus pour forcer le basculement
 * en SOLD ou UNSOLD et créditer le vendeur si nécessaire.
 */
@Component
@RequiredArgsConstructor
public class LotStatusScheduler {

    private final LotRepository lotRepository;
    private final LotServiceImpl lotService; // on a besoin d'appeler refreshLotStatus(...)

    /**
     * Toutes les minutes, on récupère les lots IN_PROGRESS dont endDate < maintenant
     * et on appelle refreshLotStatus(...) pour chacun.
     *
     * On pourrait ajuster la fréquence (cron) si on veut plus ou moins souvent.
     */
    @Scheduled(cron = "0 * * * * *") // toutes les minutes à 0 seconde
    @Transactional
    public void updateExpiredLotsStatus() {
        LocalDateTime now = LocalDateTime.now();
        // Trouver tous les lots encore marqués IN_PROGRESS mais dont endDate est dépassé
        List<Lot> expiredInProgress = lotRepository.findAllByStatusAndEndDateBefore(LotStatus.IN_PROGRESS, now);

        if (!expiredInProgress.isEmpty()) {
            for (Lot lot : expiredInProgress) {
                lotService.refreshLotStatus(lot);
            }
        }
    }
}
