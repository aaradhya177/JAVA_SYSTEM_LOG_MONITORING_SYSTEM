FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

# Install wget for downloading dependencies
RUN apk add --no-cache wget

# Create necessary directories
RUN mkdir -p /app/lib /app/bin /app/src

# Copy source files
COPY src/ /app/src/
COPY db/ /app/db/

# Download PostgreSQL JDBC driver
RUN wget -q -O /app/lib/postgresql-42.7.1.jar \
    https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar

# Compile Java sources
RUN javac -d /app/bin -cp "/app/lib/*" \
    /app/src/com/logmonitor/model/*.java \
    /app/src/com/logmonitor/util/*.java \
    /app/src/com/logmonitor/dao/*.java \
    /app/src/com/logmonitor/*.java

# Run the application
CMD ["java", "-cp", "/app/bin:/app/lib/*", "com.logmonitor.Main"]
