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
     * Utilisé pour le nettoyage des lots vendus ou invendus plus anciens que X jours.
     */
    List<Lot> findAllByStatusInAndEndDateBefore(List<LotStatus> statuses, LocalDateTime cutoff);

    /**
     * Récupère tous les lots dont l’utilisateur propriétaire a pour nom d’utilisateur celui fourni.
     */
    List<Lot> findByOwnerUsername(String username);
}
