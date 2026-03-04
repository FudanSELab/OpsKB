# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/xidian/kg`: Spring Boot entry point `KgApplication` plus layered packages (`controller`, `service`, `dao`, `entity`, `util`). Keep new code within existing module boundaries.
- `src/main/resources`: service configuration (`application.properties`) and bundled automation assets under `dify/` and localized reference files; treat binary assets as read-only.
- `src/test/java/com/xidian/kg`: integration-style Spring Boot tests. Mirror package structure when adding coverage.
- `target/`: Maven build output; do not commit.

## Build, Test, and Development Commands
- `mvn clean package`: compile, run tests, and build the runnable JAR in `target/`.
- `mvn spring-boot:run`: start the API locally on `http://localhost:8052`.
- `mvn test`: execute the test suite; prefer running before every PR.
- `mvn dependency:tree`: optional sanity check for dependency changes.

## Coding Style & Naming Conventions
- Java 8 with Spring Web + Neo4j; use 4-space indentation, `UpperCamelCase` for classes/resources, `lowerCamelCase` for members.
- Controllers expose REST endpoints; reuse response wrappers like `controller.util.Result`.
- Services and DAOs live in mirrored `service/Impl` and `dao/impl` packages. Follow existing interface + implementation pattern.
- Prefer Lombok annotations already in use; otherwise supply explicit getters/setters.

## Testing Guidelines
- Use JUnit 5 via `spring-boot-starter-test`; extend `KgApplicationTests` or create new `*Tests.java` classes under matching packages.
- Target meaningful coverage around Neo4j interactions by mocking repositories or using embedded test databases.
- For regression cases, document setup in test comments and assert JSON payloads returned by controllers.

## Commit & Pull Request Guidelines
- Commit messages in history use concise Mandarin action phrases (e.g., “加入了分解步骤”); keep that tone while clearly stating scope.
- Group related changes per commit; include module hints like `controller` or `dao`.
- Pull requests should summarize intent, list primary endpoints affected, and link tracking issues. Add screenshots or sample JSON when updating API responses.

## Configuration & Security Notes
- Update `application.properties` only when necessary; prefer environment overrides for credentials like `spring.data.neo4j.password`.
- Large resource files in `src/main/resources/dify/` and the localized asset folder are shared artifacts—coordinate before replacing them.
