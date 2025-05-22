# Glich Poker - Backend

## Introduction

Welcome to the Glich Poker backend! This project provides the server-side logic and API for a multiplayer online poker application. 
The poker applicaiton supports normal poker, but also poker with special rules to make the game feel more dynamic.

Among other, the special rules depend on the current weather at the location of the lobby owner. This hopefully makes the game more fun as it is now somewhat depended on factors outside our control. (But the special rules can also always be disabled)

## Technologies Used

*   **Java 17**: Core programming language.
*   **Spring Boot**: Framework for creating RESTful web services and managing application context.
    *   **Spring MVC**: For handling HTTP requests.
    *   **Spring Data JPA**: For database interaction and ORM.
    *   **Spring WebSocket**: For real-time communication with clients.
    *   **Spring Security**: (Potentially, for more advanced authentication/authorization - can be added if used).
*   **Hibernate**: JPA implementation.
*   **H2 Database**: In-memory database for development and testing.
*   **PostgreSQL**: (Recommended) Relational database for production.
*   **Gradle**: Build automation tool.
*   **JUnit 5 & Mockito**: For unit and integration testing.
*   **Lombok**: To reduce boilerplate code (e.g., getters, setters).
*   **MapStruct**: For DTO to Entity mapping.

## High-Level Components

The backend is structured into several key components that work together to deliver the application's functionality:

1.  **API Controllers (`src/main/java/ch/uzh/ifi/hase/soprafs24/controller`)**
    *   **Role**: These are the entry points for all client requests. They handle incoming HTTP and WebSocket messages, validate them, and delegate the processing to the appropriate service layer components.
    *   **Key Classes**:
        *   [`UserController.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserController.java): Manages user registration, login, logout, and profile updates.
        *   [`GameController.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/GameController.java): Handles RESTful requests related to game creation, listing, joining, and fetching game details.
        *   [`WebSocketController.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/WebSocketController.java): Manages real-time game actions (e.g., bet, fold, check) received over WebSocket connections.

2.  **Service Layer (`src/main/java/ch/uzh/ifi/hase/soprafs24/service`)**
    *   **Role**: This layer contains the core business logic of the application. Services orchestrate operations, interact with repositories for data persistence, and manage complex workflows.
    *   **Key Classes**:
        *   [`UserService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java): Implements logic for user management.
        *   [`GameService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/GameService.java): Manages the lifecycle of poker games, including creation, player management, and settings.
        *   [`PlayerService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/PlayerService.java): Manages players and their current state in the game.
        *   [`PlayerStatisticsService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/PlayerStatisticsService.java): Calculates and manages player statistics.
        *   [`InviteGameService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/InviteGameService.java): Handles game invitations.

3.  **Data Persistence (Repositories & Entities)**
    *   **Role**: Manages all interactions with the database. Repositories provide an abstraction layer over data access, while entities define the structure of the data.
    *   **Repositories (`src/main/java/ch/uzh/ifi/hase/soprafs24/repository`)**: Interfaces extending Spring Data JPA's `JpaRepository` for CRUD operations and custom queries.
        *   E.g., [`UserRepository.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/UserRepository.java), [`GameRepository.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository/GameRepository.java).
    *   **Entities (`src/main/java/ch/uzh/ifi/hase/soprafs24/entity`)**: Plain Old Java Objects (POJOs) annotated with JPA annotations to map to database tables.
        *   E.g., [`User.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/User.java), [`Game.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Game.java), [`Player.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Player.java).

4.  **Real-time Communication (WebSockets)**
    *   **Role**: Enables bi-directional, real-time communication between the server and clients for instant game updates and player actions.
    *   **Key Components**:
        *   `WebSocketConfig.java`: Configures WebSocket endpoints and message brokers.
        *   [`WebSocketHander.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller/WebSocketHandler.java): Handle receiving messages from clients and broadcasting messages to clients.

These components are interconnected: Controllers receive requests and call Services. Services use Repositories to interact with Entities (data) and may also use other services to fulfill requests. For real-time aspects, WebSocket messages are handled by the `WebsocketHandler` to notify clients.

## Launch & Deployment

Follow these steps to get the Glich Poker backend up and running locally.

### Prerequisites

*   **Java 17 SDK**: Ensure Java 17 is installed and your `JAVA_HOME` environment variable is set correctly.
*   **Git**: For cloning the repository.
*   **(Optional) PostgreSQL**: If you intend to run with a production-like database. For local development, the application defaults to an H2 in-memory database.

### Getting Started

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd Glich_Poker_Backend
    ```

2.  **Build the application:**
    You can use the local Gradle Wrapper.
    *   macOS/Linux: `./gradlew build`
    *   Windows: `.\gradlew.bat build`

3.  **Run the application:**
    *   macOS/Linux: `./gradlew bootRun`
    *   Windows: `.\gradlew.bat bootRun`
    The server will start, typically on `localhost:8080`. You can verify by visiting this address in your browser or checking the console output.

4.  **Run tests:**
    *   macOS/Linux: `./gradlew test`
    *   Windows: `.\gradlew.bat test`
    This will execute all unit and integration tests. Test reports can be found in `build/reports/tests/test/index.html`.

### Database Configuration

*   **Development (Default)**: The application is configured to use an H2 in-memory database by default (see `src/main/resources/application-dev.properties` or `src/test/resources/application-test.properties`). No external database setup is required for basic local development and testing.
*   **Production**: For a persistent data store, you should configure a relational database like PostgreSQL. You'll need to:
    1.  Install and run a PostgreSQL server.
    2.  Create a database for the application.
    3.  Update the database connection properties in `src/main/resources/application-prod.properties` (you might need to create this file or use Spring profiles to manage configurations).
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/glich_poker_db
        spring.datasource.username=your_db_user
        spring.datasource.password=your_db_password
        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
        spring.jpa.hibernate.ddl-auto=update # Or validate for production
        ```
    4.  Run the application with the production profile activated (e.g., by setting the `SPRING_PROFILES_ACTIVE=prod` environment variable).

### Development Mode (Hot Reloading)

For a smoother development experience with automatic reloading on file changes:
1.  Open two terminal windows.
2.  In the first terminal, run:
    *   macOS/Linux: `./gradlew build --continuous -xtest` (builds continuously, skips tests for speed)
    *   Windows: `.\gradlew.bat build --continuous -xtest`
3.  In the second terminal, run:
    *   macOS/Linux: `./gradlew bootRun`
    *   Windows: `.\gradlew.bat bootRun`

### Releases

(This section can be expanded based on your team's specific release process.)
A typical release process might involve:
1.  Ensuring all tests pass on the main branch
2.  By pushing the application to main, it is automatically deployed to google cloud through github actions.

## Roadmap (Future Features)

Here are a few features that new developers could contribute to:

1.  **Customizable Game Rules**: Allow game hosts to customize more aspects of the game, such as blind structures, antes, or special game variants.
2.  more?

## Authors and Acknowledgment

| Contributer | Main focus |
|--|--|
| Noé Brunner | Frontend |
| Ah Reum Oh | Frontend |
| Gian Gerber | Backend |
| Elio Kuster | Backend |

We would like to acknowledge [Name of University/Professor/Course - if applicable] for their guidance and support during the development of this project as part of [Course Name - if applicable].

This project was initially based on a [Spring Boot template](https://github.com/HASEL-UZH/sopra-fs25-template-server) provided by the University of Zürich.

## License

This project is licensed under the `GNU General Public License v3.0`. It includes material originally licensed under the `Apache License, Version 2.0` from [HASEL-UZH / sopra-fs25-template-server](https://github.com/HASEL-UZH/sopra-fs25-template-server).

See [GPL-3.0-LICENSE.md](GPL-3.0-LICENSE.md) and [APACHE-2.0-LICENSE.md](APACHE-2.0-LICENSE.md) for details.