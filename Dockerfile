# Multi-stage Dockerfile for Production

# Build stage
FROM maven:3.9-eclipse-temurin-17 as build
WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# Runtime stage
FROM eclipse-temurin:17-jre-alpine as production

# Create non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Development stage (for local development)
FROM eclipse-temurin:17-jdk-alpine as development

RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

RUN apk add --no-cache curl maven

WORKDIR /app

USER spring

EXPOSE 8080 5005

ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

CMD ["./mvnw", "spring-boot:run"]