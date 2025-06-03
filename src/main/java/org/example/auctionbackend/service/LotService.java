package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.CreateLotRequestDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.dto.UpdateLotRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface LotService {

    /**
     * Liste paginée des lots, éventuellement filtrés par catégorie.
     *
     * @param categoryId identifiant de la catégorie (null pour toutes)
     * @param page       numéro de page (0-based)
     * @param size       taille de page
     * @return page de LotDTO
     */
    Page<LotDTO> listByCategory(Long categoryId, int page, int size);

    /**
     * Récupère le détail d’un lot par son id.
     *
     * @param id identifiant du lot
     * @return Optional contenant LotDetailDTO si trouvé
     */
    Optional<LotDetailDTO> findById(Long id);

    /**
     * Récupère les derniers lots créés (triés par id décroissant).
     *
     * @param count nombre de lots à récupérer
     * @return liste de LotDTO
     */
    List<LotDTO> findLatestLots(int count);

    /**
     * Récupère la liste des lots suivis (enchéris) par un utilisateur.
     *
     * @param username nom d'utilisateur
     * @return liste de LotDTO
     */
    List<LotDTO> getFollowedLots(String username);

    /**
     * Récupère la liste des lots créés par l'utilisateur connecté.
     *
     * @param username nom d'utilisateur
     * @return liste de LotDTO
     */
    List<LotDTO> getUserLots(String username);

    /**
     * Crée un nouveau lot (status = PENDING) pour l'utilisateur connecté.
     *
     * @param req DTO de création
     * @return LotDTO du lot créé
     */
    LotDTO createLot(CreateLotRequestDTO req);

    /**
     * Annule un lot en cours d'enchère. Seul le propriétaire peut annuler.
     * Rembourse le dernier enchérisseur si nécessaire et supprime le lot.
     *
     * @param username nom du propriétaire
     * @param lotId    identifiant du lot
     */
    void cancelLot(String username, Long lotId);

    /**
     * Met à jour un lot dont l'enchère est terminée et invendue (UNSOLD).
     * Seul le propriétaire peut modifier. Remet le lot en statut PENDING.
     *
     * @param username nom du propriétaire
     * @param lotId    identifiant du lot
     * @param req      DTO contenant les nouveaux champs
     * @return LotDTO mis à jour
     */
    LotDTO updateLot(String username, Long lotId, UpdateLotRequestDTO req);

    /**
     * Remet en vente un lot déjà invendu (UNSOLD). Seul le propriétaire peut relister.
     * Met à jour dates et remets status à PENDING.
     *
     * @param username nom du propriétaire
     * @param lotId    identifiant du lot
     * @param req      DTO contenant les champs (title, description, prix, dates, category)
     * @return LotDTO remis en vente
     */
    LotDTO relistLot(String username, Long lotId, UpdateLotRequestDTO req);
}
