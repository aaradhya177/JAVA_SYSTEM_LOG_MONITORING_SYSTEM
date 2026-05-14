# Build stage - compile Java code
FROM eclipse-temurin:11-jdk-alpine as builder

WORKDIR /build

# Install wget
RUN apk add --no-cache wget

# Create directories
RUN mkdir -p /build/lib /build/bin

# Copy source files
COPY src/ /build/src/

# Download PostgreSQL JDBC driver
RUN wget -q -O /build/lib/postgresql-42.7.1.jar \
    https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar

# Compile Java sources
RUN javac -d /build/bin -cp "/build/lib/*" \
    /build/src/com/logmonitor/model/*.java \
    /build/src/com/logmonitor/util/*.java \
    /build/src/com/logmonitor/dao/*.java \
    /build/src/com/logmonitor/service/*.java \
    /build/src/com/logmonitor/server/*.java \
    /build/src/com/logmonitor/ui/*.java \
    /build/src/com/logmonitor/*.java

# Runtime stage - minimal image with only JRE
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

ENV PORT=10000

# Copy compiled classes from builder
COPY --from=builder /build/bin /app/bin/

# Copy dependencies from builder
COPY --from=builder /build/lib /app/lib/

EXPOSE 10000

# Run the application
CMD ["java", "-cp", "/app/bin:/app/lib/*", "com.logmonitor.Main"]
