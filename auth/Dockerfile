# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first for Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn -B dependency:resolve dependency:resolve-plugins

# Copy source code
COPY src ./src

# Build fat jar
RUN mvn clean package -DskipTests -B

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built jar
COPY --from=build /app/target/auth-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]