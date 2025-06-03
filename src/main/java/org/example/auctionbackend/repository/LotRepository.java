package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.Lot;
import org.example.auctionbackend.model.LotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long> {
    /**
     * Récupère la page de lots dont les catégories (parent ou enfants) sont dans la liste.
     */
    Page<Lot> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    /**
     * Récupère les derniers lots créés (triés par id décroissant).
     */
    List<Lot> findAllByOrderByIdDesc(Pageable pageable);

    /**
     * Récupère tous les lots dont le statut fait partie de la liste et dont la date de fin est avant la date cutoff.
     * (utilisé par LotCleanupScheduler existant)
     */
    List<Lot> findAllByStatusInAndEndDateBefore(List<LotStatus> statuses, LocalDateTime cutoff);

    /**
     * Récupère tous les lots dont l’utilisateur propriétaire a pour nom d’utilisateur celui fourni.
     */
    List<Lot> findByOwnerUsername(String username);

    /**
     * NOUVEAU : récupérer tous les lots qui sont toujours IN_PROGRESS mais dont la date de fin est passée
     * → Pour pouvoir basculer ces lots en SOLD/UNSOLD via un scheduler.
     */
    List<Lot> findAllByStatusAndEndDateBefore(LotStatus status, LocalDateTime cutoff);
}
