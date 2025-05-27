package org.example.auctionbackend.controller;

import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.service.LotService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/lots")
public class LotController {

    private final LotService lotService;

    public LotController(LotService lotService) {
        this.lotService = lotService;
    }

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

}
