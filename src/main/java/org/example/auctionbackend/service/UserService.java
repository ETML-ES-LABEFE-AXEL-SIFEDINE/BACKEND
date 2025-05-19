package org.example.auctionbackend.service;

public interface UserService {
    /**
     * Créditer le compte de l'utilisateur.
     * @param username identifiant
     * @param amount montant à ajouter (>=100)
     * @return nouveau solde
     */
    Double topUp(String username, Double amount);
}
