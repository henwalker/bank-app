\# Bank App



A native Android app for bank customers to log in, view their accounts, see account details, and set an account nickname. Built with a Spring Boot backend and SQLite database.



\## Tech stack



\*\*Android:\*\* Kotlin, Jetpack Compose, MVVM (ViewModel), Retrofit + Gson, Navigation Compose. Fully native, no cross-platform frameworks.



\*\*Backend:\*\* Java 17, Spring Boot, Spring Security, JWT (jjwt), Spring Data JPA / Hibernate, SQLite. Runs locally, no cloud services.



\## Architecture



```mermaid

flowchart LR

&#x20;   subgraph Android\["Android App - Kotlin + Jetpack Compose"]

&#x20;       UI\["Compose Screens<br/>Login, Accounts, Details"] --> VM\["ViewModels"]

&#x20;       VM --> API\["Retrofit ApiClient"]

&#x20;       SM\["SessionManager<br/>JWT kept in memory"] --> API

&#x20;   end



&#x20;   API -- "HTTP JSON<br/>Authorization: Bearer JWT" --> F



&#x20;   subgraph Backend\["Spring Boot Backend"]

&#x20;       F\["JwtAuthFilter"] --> C\["Controllers<br/>AuthController, AccountController"]

&#x20;       C --> S\["AccountService"]

&#x20;       S --> R\["JPA Repositories"]

&#x20;   end



&#x20;   R --> DB\[("SQLite<br/>bank.db")]

```



\## Application communication



```mermaid

sequenceDiagram

&#x20;   actor U as Customer

&#x20;   participant A as Android App

&#x20;   participant B as Spring Boot API

&#x20;   participant D as SQLite



&#x20;   U->>A: Enter username and password

&#x20;   A->>B: POST /api/auth/login

&#x20;   B->>D: Find customer by username

&#x20;   D-->>B: Customer and password hash

&#x20;   B->>B: Verify BCrypt hash, sign JWT

&#x20;   B-->>A: 200 token and customerNumber

&#x20;   A->>A: Keep token in memory



&#x20;   U->>A: Open accounts

&#x20;   A->>B: GET /api/accounts with Bearer token

&#x20;   B->>B: Validate JWT, read customer number

&#x20;   B->>D: Select accounts for this customer

&#x20;   B-->>A: 200 list of accounts



&#x20;   U->>A: Open an account

&#x20;   A->>B: GET /api/accounts/{accountNumber}

&#x20;   B->>D: Select account owned by this customer

&#x20;   B-->>A: 200 account details



&#x20;   U->>A: Save nickname

&#x20;   A->>B: PATCH /api/accounts/{accountNumber}/nickname

&#x20;   B->>D: Check ownership, update nickname

&#x20;   B-->>A: 200 updated account



&#x20;   Note over A,B: Missing or expired token returns 401 and the app goes back to login.<br/>Requesting another customer's account returns 404.

```



\## Data model



| Field | Type | Length | Rule |

|---|---|---|---|

| Customer Number | Integer | 9 | Primary key of customers, unique per customer |

| Account Number | String | 9 | Primary key of accounts, unique globally |

| Balance | Float | - | |

| Nickname | String | 255 | Optional, validated on app and server |



Passwords are stored as BCrypt hashes, never plain text.



\## API



| Method | Endpoint | Auth | Description |

|---|---|---|---|

| POST | /api/auth/login | Public | Returns a JWT for valid credentials |

| GET | /api/accounts | JWT | Lists the logged-in customer's accounts |

| GET | /api/accounts/{accountNumber} | JWT | Account details, only if owned by the customer |

| PATCH | /api/accounts/{accountNumber}/nickname | JWT | Updates the nickname (max 255 characters) |



\## Security decisions



\- All account endpoints require a valid JWT. The token is signed and expires after 1 hour.

\- The customer number is always taken from the token, never from the request, so a customer cannot access another customer's accounts by changing the account number in the URL.

\- The server is stateless (no sessions). Each request is authenticated by its token.

\- The app keeps the token in memory only, so closing the app logs the user out.

\- Plain HTTP is allowed only for local development addresses through a network security config. Production would use HTTPS only.



\## How to run



\*\*Requirements:\*\* JDK 17, Android Studio, an Android emulator.



\*\*1. Start the backend\*\*



```

cd backend

./mvnw spring-boot:run        (Windows: .\\mvnw.cmd spring-boot:run)

```



The SQLite database file `bank.db` is created automatically and seeded with test data on first run.



\*\*2. Connect the emulator to the backend\*\*



With the emulator running:



```

adb reverse tcp:8080 tcp:8080

```



This forwards the emulator's `localhost:8080` to the computer's backend. Run it again if the emulator restarts.



\*\*3. Run the app\*\*



Open the `android` folder in Android Studio and press Run.



\## Test accounts



| Username | Password | Accounts |

|---|---|---|

| ahmad | password123 | 200000001, 200000002 |

| sara | password123 | 200000003 |



\## Notes and possible improvements



\- Balance uses Float as specified. For real money I would use BigDecimal / DECIMAL to avoid rounding errors.

\- Refresh tokens, login rate limiting and account lockout would be added for production.

\- Encrypted token storage (Android Keystore) if "remember me" is needed.

\- Unit and integration tests for the service layer and API.

