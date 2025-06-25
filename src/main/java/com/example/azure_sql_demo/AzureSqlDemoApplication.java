package com.example.azure_sql_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Cloud Native Java with Azure SQL API",
        version = "1.0",
        description = "Sample API for demonstrating cloud native Java development with Azure and SQL",
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
    )
)
public class AzureSqlDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureSqlDemoApplication.class, args);
    }
}