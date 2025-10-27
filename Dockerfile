# Multi-stage build for optimized image size
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage with minimal JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Set JVM options for memory constraints (50MB heap)
# -Xmx50m: Maximum heap size
# -Xms50m: Initial heap size
# -XX:MaxMetaspaceSize=128m: Metaspace for class metadata
# -XX:+UseSerialGC: Serial GC for low memory footprint
# -XX:+TieredCompilation: Enable tiered compilation
# -XX:TieredStopAtLevel=1: Stop at C1 compiler (faster startup, less memory)
ENV JAVA_OPTS="-Xmx50m -Xms50m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

