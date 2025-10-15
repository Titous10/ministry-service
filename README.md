# ministry-service
Spring Boot microservice to manage ministries, hierarchy and member assignments.

## Features
- Create/update ministries (assignments provided in request)
- Return potential members from member-service based on criteria (no DB writes)
- Efficient hierarchy maintenance using JdbcTemplate and recursive CTEs
- Feign + Resilience4j for member-service integration

## Build & Run
1. Configure `src/main/resources/application.yml`
2. Ensure Postgres is running and Flyway migrations will run on startup
3. `mvn clean package`
4. `java -jar target/ministry-service-0.0.1-SNAPSHOT.jar`