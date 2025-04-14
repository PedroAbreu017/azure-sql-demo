FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar o arquivo JAR da sua aplicação
COPY target/azure-sql-demo-0.0.1-SNAPSHOT.jar app.jar

# Configurações do Azure Key Vault
ENV AZURE_KEYVAULT_URI=https://java-sql-demo-kv-2025.vault.azure.net/
ENV AZURE_LOG_LEVEL=3
ENV AZURE_POD_IDENTITY_AUTHORITY_HOST=http://169.254.169.254

# Porta exposta pela aplicação
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]