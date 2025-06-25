package com.example.azure_sql_demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        String server = "java-azure-cloud-sql-new.database.windows.net";
        String database = "app-db-clean";
        String username = "SEU_USUARIO"; // SUBSTITUA AQUI
        String password = "SUA_SENHA";   // SUBSTITUA AQUI
        
        // Diferentes URLs para testar
        String[] connectionStrings = {
            // URL 1 - Padrão com SSL
            String.format("jdbc:sqlserver://%s:1433;database=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", 
                server, database),
            
            // URL 2 - SSL flexível
            String.format("jdbc:sqlserver://%s:1433;database=%s;encrypt=true;trustServerCertificate=true;loginTimeout=30;", 
                server, database),
            
            // URL 3 - Sem SSL (não recomendado para produção)
            String.format("jdbc:sqlserver://%s:1433;database=%s;encrypt=false;loginTimeout=30;", 
                server, database)
        };
        
        for (int i = 0; i < connectionStrings.length; i++) {
            System.out.println("\n=== Teste " + (i + 1) + " ===");
            System.out.println("URL: " + connectionStrings[i]);
            
            try (Connection connection = DriverManager.getConnection(connectionStrings[i], username, password)) {
                System.out.println("✅ SUCESSO! Conexão estabelecida.");
                
                // Teste simples de query
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT @@VERSION");
                if (rs.next()) {
                    System.out.println("Versão do SQL Server: " + rs.getString(1));
                }
                break; // Se chegou aqui, a conexão funciona
                
            } catch (Exception e) {
                System.err.println("❌ ERRO: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Causa: " + e.getCause().getMessage());
                }
            }
        }
    }
}