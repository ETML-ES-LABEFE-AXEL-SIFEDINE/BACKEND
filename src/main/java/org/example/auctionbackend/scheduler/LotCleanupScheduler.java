package org.example.auctionbackend.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.model.Lot;
import org.example.auctionbackend.model.LotStatus;
import org.example.auctionbackend.repository.LotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LotCleanupScheduler {

    private final LotRepository lotRepository;

    /**
     * Tous les jours à 2h du matin (heure du serveur), supprime les lots SOLD ou UNSOLD
     * dont la date de fin est supérieure à 7 jours.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldLots() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        // Récupère tous les lots dont le statut est SOLD ou UNSOLD et dont endDate est < cutoff
        List<Lot> toDelete = lotRepository.findAllByStatusInAndEndDateBefore(
                List.of(LotStatus.SOLD, LotStatus.UNSOLD),
                cutoff
        );

        if (!toDelete.isEmpty()) {
            lotRepository.deleteAll(toDelete);
        }
    }
}
