FROM openjdk:11-jre-slim

WORKDIR /app

# Copy lib folder with all dependencies
COPY lib/ /app/lib/

# Copy compiled classes
COPY bin/ /app/bin/

# Run the application
CMD ["java", "-cp", "/app/bin:/app/lib/*", "com.logmonitor.Main"]
