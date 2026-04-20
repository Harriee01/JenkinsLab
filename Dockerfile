# ===========================================================================
# Dockerfile – Fake Store API REST Assured Test Suite
#
# Multi-stage build:
#   Stage 1 (builder) – downloads Maven dependencies and compiles/runs tests.
#   Stage 2 (report)  – lightweight image that serves the Allure HTML report.
#
# Usage:
#   # Build image and run tests:
#   docker build --target builder -t fakestore-tests .
#   docker run --rm -v "$(pwd)/target:/workspace/target" fakestore-tests
#
#   # Build the full image (includes Allure report generation):
#   docker build -t fakestore-tests-report .
#   docker run --rm -p 8080:8080 fakestore-tests-report
#   # Then open http://localhost:8080 in your browser.
# ===========================================================================

# ---------------------------------------------------------------------------
# Stage 1 – Builder
# Uses the official Maven image with Java 21 (Temurin JDK, Alpine-based)
# ---------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

LABEL maintainer="QA Automation Team"
LABEL description="Fake Store API REST Assured test suite – Maven build + test execution stage"

# Working directory inside the container
WORKDIR /workspace

# ── Dependency layer (cached unless pom.xml changes) ──────────────────────
# Copy only pom.xml first so Docker caches this heavy layer separately.
COPY pom.xml .

# Download all Maven dependencies without running the build.
# The '-B' flag activates batch mode (no ANSI decorations; cleaner CI logs).
RUN mvn dependency:go-offline -B

# ── Source layer ────────────────────────────────────────────────────────────
# Now copy the full source tree. Changes here do NOT invalidate the dep cache.
COPY src ./src

# ── Run tests ───────────────────────────────────────────────────────────────
# Skip the AspectJ compiler plugin warnings and run with fail-at-end so all
# tests execute even if some fail (results are still written to allure-results).
RUN mvn clean test -B \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven=WARN \
    || true
# "|| true" ensures the container exits 0 so Docker doesn't abort the build
# before we can collect the allure-results artefacts in Stage 2.

# ---------------------------------------------------------------------------
# Stage 2 – Report Server
# Installs Allure CLI and serves the generated report over HTTP.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS report

LABEL description="Fake Store API – Allure report server"

# Install curl (needed to fetch Allure release) and bash
RUN apk add --no-cache bash curl unzip

# ── Install Allure CLI ──────────────────────────────────────────────────────
ARG ALLURE_VERSION=2.29.1
RUN curl -sSL \
    "https://github.com/allure-framework/allure2/releases/download/${ALLURE_VERSION}/allure-${ALLURE_VERSION}.zip" \
    -o /tmp/allure.zip \
    && unzip -q /tmp/allure.zip -d /opt \
    && rm /tmp/allure.zip \
    && ln -s /opt/allure-${ALLURE_VERSION}/bin/allure /usr/local/bin/allure

# Copy allure-results from the builder stage
WORKDIR /report
COPY --from=builder /workspace/target/allure-results ./allure-results

# Generate the static HTML report at container start time, then serve it.
# Port 8080 is exposed for the built-in Allure Jetty server.
EXPOSE 8080

# Entrypoint: generate report from results and open server on 0.0.0.0:8080
CMD ["sh", "-c", \
     "allure generate allure-results --clean -o allure-report && \
      allure open allure-report --host 0.0.0.0 --port 8080"]
