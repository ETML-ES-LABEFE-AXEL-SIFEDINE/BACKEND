package org.example.auctionbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.CreateLotRequestDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.service.LotService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotService lotService;

    @GetMapping
    public Page<LotDTO> listLots(
            @RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return lotService.listByCategory(categoryId, page, size);
    }

    /**
     * GET /api/lots/{id}
     * Renvoie 200 + LotDetailDTO si trouvé, sinon 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LotDetailDTO> getLot(@PathVariable Long id) {
        return lotService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public List<LotDTO> getRecentLots() {
        return lotService.findLatestLots(8); // 8 derniers lots
    }

    /**
     * POST /api/v1/lots
     * Crée un nouveau lot (status = PENDING).
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotDTO> createLot(
            @Valid @RequestBody CreateLotRequestDTO req) {
        LotDTO dto = lotService.createLot(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

}
