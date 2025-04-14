# Azure SQL Spring Boot Demo

Uma aplicação Java Spring Boot com Azure SQL Database, implantada como contêiner Docker no Azure App Service.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.1.x
- Spring Data JPA
- Azure SQL Database
- Docker
- Azure Container Registry
- Azure App Service
- Swagger/OpenAPI

## Funcionalidades

- API RESTful para gerenciamento de produtos (operações CRUD)
- Utilitários para carregamento de dados de teste
- Documentação da API com Swagger

## Configuração do Ambiente

### Pré-requisitos
- Java Development Kit (JDK) 17
- Maven
- Docker
- Azure CLI

### Desenvolvimento Local
1. Clone o repositório
2. Copie `src/main/java/com/example/azure_sql_demo/config/KeyVaultConfigTemplate.java` para `KeyVaultConfig.java` no mesmo diretório
3. Atualize as credenciais do banco de dados no arquivo copiado
4. Execute `mvn spring-boot:run`

### Implantação no Azure
1. Compile a aplicação: `mvn clean package -DskipTests`
2. Construa a imagem Docker: `docker build -t <acr-name>.azurecr.io/azure-sql-demo:v1 .`
3. Envie para o Azure Container Registry: `docker push <acr-name>.azurecr.io/azure-sql-demo:v1`
4. Implante no Azure App Service usando o Portal do Azure ou CLI

## Endpoints da API

### Products API
- `GET /api/products` - Obter todos os produtos
- `GET /api/products/{id}` - Obter um produto específico
- `POST /api/products` - Criar um novo produto
- `PUT /api/products/{id}` - Atualizar um produto
- `DELETE /api/products/{id}` - Excluir um produto

### Admin API
- `POST /api/admin/load-sample-data` - Carregar dados de amostra aleatórios
- `POST /api/admin/load-predefined-data` - Carregar produtos predefinidos
- `DELETE /api/admin/clear-data` - Limpar todos os dados

## Licença
Este projeto está licenciado sob a licença MIT - consulte o arquivo LICENSE para detalhes.