# Auction Backend

## 🚀 Introduction

Ce projet est une API RESTful pour une plateforme de vente aux enchères en ligne.
Développé en Java 17+ avec Spring Boot, MySQL, et sécurisé par JWT (JSON Web Token).

---

## 📌 Fonctionnalités

* Gestion des utilisateurs (inscription, connexion, changement de mot de passe, profil)
* Authentification sécurisée avec JWT (Access Token & Refresh Token)
* Gestion des catégories avec arborescence (catégories et sous-catégories)
* Gestion des lots (enchères) avec pagination
* Système de verrouillage de compte après plusieurs échecs de connexion
* Suivi des enchères et aprovisonnement du solde et historique des transactions

---
## 📚 Documentation

Les diagrammes se trouvent dans le dossier [`docs/`](docs/) :

- **Schéma relationnel (ER)**  
  [`docs/schema.puml`](docs/schema.puml)  

- **Diagramme de classes UML**  
  [`docs/class-diagram.puml`](docs/class-diagram.puml)  

---

## ✅ Prérequis

| Outil | Version minimale |
| ----- | ---------------- |
| Java  | 17               |
| Maven | 3.6+             |
| MySQL | 8+               |
| Git   | 2.20+            |

---

## ⚙️ Installation

1. **Cloner le dépôt :**

   ```bash
   git clone <URL-du-repo>
   cd auction-backend
   ```

2. **Configurer la base de données**
   Dans `src/main/resources/application.properties` ou `application.yml` :

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/auction_db?allowPublicKeyRetrieval=true&useSSL=false
   spring.datasource.username=root
   spring.datasource.password=<VOTRE_MOT_DE_PASSE>
   ```

3. **Lancer l’application en mode développement :**

   ```bash
   mvn spring-boot:run
   ```

   Elle tourne alors sur `http://localhost:8080` par défaut.


---


## 🔑 Configuration JWT

* Clé et durées dans `JwtUtils`.
* Stockez-les en variables d’environnement :

  ```properties
  jwt.secret=${JWT_SECRET}
  jwt.accessTokenExpirationMs=${JWT_ACCESS_EXPIRATION}
  jwt.refreshTokenExpirationMs=${JWT_REFRESH_EXPIRATION}
  ```

---

## 📊 API Endpoints

| Méthode | Endpoint                  | Description                         |
| ------- | ------------------------- | ----------------------------------- |
| POST    | `/api/auth/register`      | Inscription d’un nouvel utilisateur |
| POST    | `/api/auth/login`         | Connexion (JWT)                     |
| POST    | `/api/auth/refresh`       | Renouveler les tokens JWT           |
| GET     | `/api/auth/user/me`       | Profil utilisateur (authentifié)    |
| GET     | `/api/categories`         | Liste plate de catégories           |
| GET     | `/api/categories/tree`    | Arborescence des catégories         |
| GET     | `/api/lots`               | Liste paginée des lots              |
| GET     | `/api/lots/{id}`          | Détail d’un lot                     |
| GET     | `/api/lots/recent`        | Derniers lots ajoutés               |
| POST    | `/api/lots/{id}/bids`     | Placer une enchère (en cours)       |
| GET     | `/api/user/followed-lots` | Lots suivis                         |
| POST    | `/api/user/top-up`        | Approvisionnement du compte         |

---

## 📌 Gestion des erreurs

* **GlobalExceptionHandler** centralise les réponses d’erreur.
* Codes HTTP & messages clairs :

  * **400 Bad Request** : validation, règles métier
  * **401 Unauthorized** : token manquant/invalide
  * **403 Forbidden** : accès non autorisé
  * **404 Not Found** : ressource inexistante
  * **500 Internal Server Error** : erreur interne

---

## 📝 Licence

Sous licence **MIT**
