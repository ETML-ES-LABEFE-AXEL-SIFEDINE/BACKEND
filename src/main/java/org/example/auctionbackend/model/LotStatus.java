package org.example.auctionbackend.model;

/**
 * Statut d’un lot d’enchères.
 */
public enum LotStatus {
    /** Enchère en cours (on peut encore enchérir) */
    IN_PROGRESS,
    /** Enchère terminée sans adjudication (invendu) */
    UNSOLD,
    /** Enchère terminée et objet adjugé (vendu) */
    SOLD,
    /** Lot créé mais pas encore ouvert à l’enchère */
    PENDING
}
