# syntax=docker/dockerfile:1
# Multi-stage build. Works identically with `docker build` and `podman build`.

# ---- Stage 1: build & test with a full JDK 25 + Maven ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Cache dependencies first: copy only the POM, resolve, then copy sources.
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

COPY src ./src
RUN mvn -q -e -B clean package

# ---- Stage 2: slim runtime with just a JRE ----
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Run as a non-root user.
RUN useradd --system --create-home --uid 10001 appuser
USER appuser

COPY --from=build /app/target/actor-ledger.jar ./actor-ledger.jar

# Default demo run; override args at `docker run` time, e.g. `... 2000 1000000`.
ENTRYPOINT ["java", "-jar", "/app/actor-ledger.jar"]
CMD ["1000", "500000"]
