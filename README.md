
## 🚀 Introduction

This project is a **RESTful API** for an online auction platform.
It is built with **Java 17**, **Spring Boot 3.4.5**, **MySQL 8** and secured with **JWT**.

---

## 📌 Features

* User management (sign‑up, login, password change, profile)
* JWT‑based authentication (access + refresh tokens)
* Category tree management (parent / sub‑categories)
* Lot (auction) management with pagination & filtering
* Account lock‑out after repeated failed logins
* Bid tracking, balance top‑up and transaction history

---

## 📚 Project documentation

PlantUML diagrams are stored in [`docs/`](docs/):

| Diagram                  | File                                                 |
| ------------------------ | ---------------------------------------------------- |
| Entity‑relationship (ER) | [`docs/schema.puml`](docs/schema.puml)               |
| UML class diagram        | [`docs/class-diagram.puml`](docs/class-diagram.puml) |

---

## ✅ Prerequisites

| Tool  | Minimum version |
| ----- | --------------- |
| Java  | 17              |
| Maven | 3.6             |
| MySQL | 8               |

---

## ⚙️ Local installation (dev profile)

1. **Clone the repo**

   ```bash
   git clone <repo‑url>
   cd auction-backend
   ```
2. **Configure the database** – edit `src/main/resources/application.properties` (or use env vars):

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/auction_db?allowPublicKeyRetrieval=true&useSSL=false
   spring.datasource.username=<YOUR_USERNAME>
   spring.datasource.password=<YOUR_PASSWORD>
   ```
3. **Run the API**



   The API is now available at **`http://localhost:8000/api/v1`**.

> ℹ️  The version prefix `/api/v1` is injected globally via
>
> ```properties
> server.servlet.context-path=/api/v1
> ```
>

---

## 🔑 JWT configuration

```properties
jwt.secret-key=${JWT_SECRET_KEY:change-me-in-dev}
jwt.access-expiration-ms=${JWT_ACCESS_EXP_MS:900000}
jwt.refresh-expiration-ms=${JWT_REFRESH_EXP_MS:604800000}
```

---

## 📊 API Endpoints (v1)

| Method              | Endpoint                                | Description                                               |
|---------------------| --------------------------------------- |-----------------------------------------------------------|
| **Auth / Sessions** |                                         |                                                           |
| POST                | `/api/v1/sessions`                      | Login – create session (returns JWTs)                     |
| POST                | `/api/v1/users`                         | User registration                                         |
| POST                | `/api/v1/tokens`                        | Refresh access token                                      |
| GET                 | `/api/v1/users/me`                      | Current user profile                                      |
| POST                | `/api/v1/users/me/password`             | Change password                                           |
| **Categories**      |                                         |                                                           |
| GET                 | `/api/v1/categories`                    | Top‑level categories                                      |
| GET                 | `/api/v1/categories/{id}/subcategories` | Sub‑categories of *id*                                    |
| GET                 | `/api/v1/categories/tree`               | Full category tree                                        |
| **Lots**            |                                         |                                                           |
| GET                 | `/api/v1/lots`                          | Paginated lots (query params: `category`, `page`, `size`) |
| GET                 | `/api/v1/lots/{id}`                     | Lot details                                               |
| GET                 | `/api/v1/lots/recent`                   | Latest 8 lots                                             |
| POST                | `/api/v1/lots`                          | Create a new lot                                          |
| POST                | `/api/v1/lots/{id}/bids`                | Place a bid on lot *id*                                   |
| **User account**    |                                         |                                                           |
| POST                | `/api/v1/user/top-up`                   | Add funds to balance                                      |
| GET                 | `/api/v1/user/transactions`             | Transaction history                                       |
| GET                 | `/api/v1/user/followed-lots`            | Lots the user follows                                     |

---

## 📌 Error handling

Errors are centralised in **`GlobalExceptionHandler`** and follow these conventions:

| Status                    | Reason                                |
| ------------------------- | ------------------------------------- |
| 400 Bad Request           | Validation or business rule violation |
| 401 Unauthorized          | Missing / invalid token               |
| 403 Forbidden             | Authenticated but not allowed         |
| 404 Not Found             | Resource doesn’t exist                |
| 500 Internal Server Error | Unexpected server failure             |

---

## 📝 Licence

Released under the **MIT** licence.
