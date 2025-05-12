# Auction Backend

## 🚀 Introduction

Ce projet est une API RESTful pour une plateforme de vente aux enchères en ligne. Il est développé en Java avec Spring Boot et MySQL, et utilise JWT (JSON Web Token) pour l'authentification et la sécurité.

## 📌 Fonctionnalités

* Gestion des utilisateurs (inscription, connexion, changement de mot de passe, profil utilisateur)
* Authentification sécurisée avec JWT (Access Token et Refresh Token)
* Gestion des catégories avec arborescence (catégories et sous-catégories)
* Gestion des lots (enchères) avec pagination
* Système de verrouillage de compte après plusieurs échecs de connexion

## ✅ Prérequis

* Java 17+
* Maven
* MySQL
* Git

## ⚙️ Installation

1. Clonez le dépôt :

   ```bash
   git clone <URL-du-repo>
   cd auction-backend
   ```

2. Configurez votre base de données MySQL dans `application.yml` ou `application.properties` :

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/auction_db?allowPublicKeyRetrieval=true&useSSL=false
   spring.datasource.username=root
   spring.datasource.password=<VOTRE_MDP>
   ```

3. Lancez l'application :

   ```bash
   mvn spring-boot:run
   ```

## 🔑 Configuration JWT

* La clé secrète JWT est définie dans `JwtUtils`.
* Par sécurité, il est recommandé de la stocker dans une variable d'environnement.

## 📊 API Endpoints

| Méthode | Endpoint               | Description                         |
| ------- | ---------------------- | ----------------------------------- |
| POST    | `/api/auth/register`   | Inscription d'un nouvel utilisateur |
| POST    | `/api/auth/login`      | Connexion de l'utilisateur (JWT)    |
| POST    | `/api/auth/refresh`    | Renouveler les tokens JWT           |
| GET     | `/api/auth/user/me`    | Profil utilisateur (authentifié)    |
| GET     | `/api/categories`      | Liste des catégories principales    |
| GET     | `/api/categories/tree` | Arborescence des catégories         |
| GET     | `/api/lots`            | Liste des lots (avec pagination)    |
| GET     | `/api/lots/{id}`       | Détail d’un lot                     |
| GET     | `/api/lots/recent`     | Derniers lots ajoutés               |

## 🚀 Sécurité et Authentification

* Utilisation de JWT pour sécuriser les endpoints.
* Les utilisateurs doivent s'authentifier pour accéder aux ressources protégées.
* Le système de verrouillage de compte est actif après plusieurs tentatives de connexion échouées.

## 📌 Gestion des Erreurs

* Gestion centralisée des erreurs avec `GlobalExceptionHandler`.
* Messages d'erreur clairs pour les erreurs de validation.

## ✅ Contribution

* Clonez le projet et créez une nouvelle branche pour vos modifications.
* Utilisez Git-flow pour la gestion des branches.
* Proposez une Pull Request avec une explication claire de vos changements.

## ⚡ Licence

Ce projet est libre d'utilisation.
