package org.example.auctionbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Représente un lot mis aux enchères.
 */
@Entity
@Table(name = "lots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Titre court ou résumé du lot, pour l’affichage en liste */
    @Column(nullable = false, length = 255)
    private String title;

    /** Description détaillée du lot */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Mise initiale du lot */
    @Column(name = "initial_price", nullable = false)
    private Double initialPrice;

    /** Date/heure de début de l’enchère */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** Date/heure de fin de l’enchère */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** Statut actuel du lot (PENDING, IN_PROGRESS, UNSOLD, SOLD) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LotStatus status;

    /** Catégorie à laquelle ce lot appartient */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // À venir : relation vers les enchères (Bid) pour calculer l’enchère courante
}
