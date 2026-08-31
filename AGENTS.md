This application is a web based search engine, intended to match people who want to participate in partner dances like slow walz, rumba, salsa, etc.
Even though the technical name of the project is "dancewithme" it public facing name is "Shall we Dance?". We may rename this sometime in the future if this sticks.

# Functionality

The core component of this application there is the SearchService, which tries to match searchable items (dancers) to search parameters.
In order to do this, each search parameter needs to have a counterpart in the searchable items.
For example: if you want to search on:
- the distance two dancers are apart, you need to know where they live.
- how often then want to dance per week, you need to know the preferred frequency.
- dance style and their roles (lead or follow) and skill in them, you need to know what they can do.

A dancers basically is an implementation of both searchable item and search parameters, 
plus some additional descriptive information like a personal text and photos.
Dancer is the only class that is a searchable item, so there is no need to explicitly use that interface.
There are more instances serving as a search parameter:
- a dancer, this allows for batch searching a notification emails.
- anonymous search, for one someone has not registered yet and some fields are unknown (and have generous defaults).
- full search, a logged in dancer is searching, but is fiddling with the search parameters without updating his profile.

Basically what happens in full search is that it is populated from the dancer's profile and he/she can fiddle with them.

# Architecture

This application uses a loosly based domain driven design (DDD) architecture.
It has the following main layers:
- **Domain layer**: contains the core business logic and domain entities like `Dancer`, along with the interfaces for searchable items and search parameters. This layer is independent of any infrastructure concerns.
- **Application layer**: contains the `SearchService` and orchestrates use cases, coordinating between the domain and infrastructure.
- **Infrastructure layer**: external facing logic, like the Vaadin based web application.

## Package layout
Root package `org.tbee.dancewithme`, entry point `Dancewithme.java`.
- `domain` - entities (`Dancer`, `DancerDancestyle`, `DancerSearchingFor`, `DancerPhoto`, `Dancestyle`, `City`, `Country`) plus `BaseEntity`, and the subpackages `repository`, `service`, `valueobject`.
- `application` - `SearchService`, `DancerService`, `EmailConfirmationService`, `PasswordResetService`, and the outbound port `EmailService`.
- `infrastructure` - `jpa` (`CustomRepositoryImpl`, `JpaConfiguration`), `mail` (`SmtpEmailService`), `vdn` (app layout, `LocaleService`) with subpackages `view`, `component`, `security`.

# Tech stack

- Java 25, Spring Boot 4 (parent), Vaadin 25 (+ viritin addon), Spring Data JPA, Spring Security, Spring Mail.
- PostgreSQL, schema managed by Liquibase.
- In-house library `org.tbee:webstack` (from nexus.softworks.nl) provides `PostgresTestContainer` and the Hibernate dialect and a number of fluent API Vaadin components.
- Frontend (Vite/TypeScript/lit) is only built by Vaadin; no manual npm work needed.

# Conventions

- The domain classes *are* the JPA entities; there is no separate persistence model to map to.
- Accessors are fluent and have no `get`/`set` prefix: `dancer.email()` reads, `dancer.email(value)` writes and returns `this`. (Java record style accessors.)
- `BaseEntity` deliberately names its predicates `entityIsNew()` / `entityIsPersisted()` instead of `is...`, so Vaadin Grid does not pick them up as columns.
- The schema is owned by Liquibase (`ddl-auto=validate`); never let Hibernate generate DDL. Add a changeset file `src/main/resources/db/changelog/changes/yyyyMMdd<letter>.xml`, author `tom`; it is picked up by `includeAll`.
- Every user visible string goes into the Vaadin translation bundles `src/main/resources/vaadin-i18n/translations[_en|_nl].properties`, using dotted keys (`search.title`). Default locale is NL; `LocaleService` handles switching via the `dancewithme-locale` cookie.
- The running TODO list lives in the header comment of `Dancewithme.java`.
- `application-default.properties` in the project root is not in git and holds local settings such as `baseUrl`.

# Building and running

- Run in development: `DancewithmeTestContainer` (profiles `dev,dev-testcontainer`, starts a PostgreSQL testcontainer, seeds data via `TestDataSeeder`; needs a Docker daemon, on Windows the Rancher Desktop npipe `DOCKER_HOST`), or `DancewithmePostgres` (profiles `dev,dev-postgres`) against a local PostgreSQL on port 5418. Both live in `src/test/java` and call `Dancewithme.main`.
- Tests: `mvnw test`. JUnit 5 with Mockito, plain unit tests without a Spring context, in `src/test/java`.
- Package: `mvn_cleanPackage.cmd`. Production build: `mvn_buildForProduction.cmd` (profile `production`, which is what triggers the frontend bundle build). Containers: `mvn_buildContainerPushLocal.cmd` / `mvn_buildContainerPushRemote.cmd`, run with `runContainer.cmd`.

# Patterns
Whenever a process uses outside services that are not available on development, like sending an email, make the process run as close as possible to the actual one.
For example: when confirming the entered email after registering a profile, on development do not immediately mark the profile as confirmed.
Instead, jump to the "email address confirmed" page that normally would be opened after clicking on the link in the email. 
With the confirmation code prefilled. 
In this way on development all the parts of the application involved in that process are visited.
The switch for this is "is the `dev` profile active", see `EmailConfirmationService.isDevelopment()`.

# Gotchas
- Dancer.active only applies to searching, login is always allowed.