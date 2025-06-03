package org.example.auctionbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.CreateLotRequestDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.dto.UpdateLotRequestDTO;
import org.example.auctionbackend.service.LotService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotService lotService;

    /**
     * GET /lots?category={id}&page={page}&size={size}
     * Liste paginée des lots, éventuellement filtrés par catégorie.
     */
    @GetMapping
    public Page<LotDTO> listLots(
            @RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return lotService.listByCategory(categoryId, page, size);
    }

    /**
     * GET /lots/{id}
     * Renvoie 200 + LotDetailDTO si trouvé, sinon 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LotDetailDTO> getLot(@PathVariable Long id) {
        return lotService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /lots/recent
     * Renvoie les 8 derniers lots créés.
     */
    @GetMapping("/recent")
    public List<LotDTO> getRecentLots() {
        return lotService.findLatestLots(8);
    }

    /**
     * GET /lots/user
     * Récupère la liste des lots créés par l’utilisateur authentifié.
     * Nécessite authentification.
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public List<LotDTO> getUserLots(Principal principal) {
        return lotService.getUserLots(principal.getName());
    }

    /**
     * POST /lots
     * Crée un nouveau lot (status = PENDING). Nécessite authentification.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotDTO> createLot(
            @Valid @RequestBody CreateLotRequestDTO req) {
        LotDTO dto = lotService.createLot(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * DELETE /lots/{id}
     * Annule un lot en cours d'enchère. Seul le propriétaire peut annuler.
     * Rembourse le dernier enchérisseur si nécessaire et supprime le lot.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelLot(@PathVariable Long id, Principal principal) {
        lotService.cancelLot(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /lots/{id}
     * Met à jour un lot invendu (status = UNSOLD). Seul le propriétaire peut modifier.
     * Remet le lot en statut PENDING après modification.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotDTO> updateLot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLotRequestDTO req,
            Principal principal
    ) {
        LotDTO updated = lotService.updateLot(principal.getName(), id, req);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /lots/{id}/relist
     * Remet en vente un lot invendu (status = UNSOLD). Seul le propriétaire peut relister.
     * Met à jour dates et remets status à PENDING.
     */
    @PostMapping("/{id}/relist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotDTO> relistLot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLotRequestDTO req,
            Principal principal
    ) {
        LotDTO relisted = lotService.relistLot(principal.getName(), id, req);
        return ResponseEntity.ok(relisted);
    }


}
