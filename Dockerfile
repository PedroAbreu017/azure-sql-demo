# Dockerfile com multi-stage build - CORRIGIDO para Flyway
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar arquivos do Maven
COPY pom.xml .
COPY src ./src

# Instalar Maven e compilar
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar o JAR do stage anterior
COPY --from=build /app/target/azure-sql-demo-0.0.1-SNAPSHOT.jar app.jar

# IMPORTANTE: NÃO PRECISA COPIAR MANUALMENTE - JAR já contém resources!
# O JAR já tem todos os arquivos db/migration dentro de BOOT-INF/classes/

# Expor porta
EXPOSE 8080

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]