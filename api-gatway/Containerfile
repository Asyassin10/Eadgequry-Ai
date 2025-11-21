# Use official OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven build artifact (jar)
COPY target/api-gatway-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8765

# Run the jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
