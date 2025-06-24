-- 🏢 ENTERPRISE INFRASTRUCTURE AS CODE
-- Script de inicialização automática do banco
-- Executado automaticamente pelo SQL Server na primeira inicialização

USE master;
GO

-- Verificar se banco já existe
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'azure_sql_demo')
BEGIN
    PRINT '🏢 ENTERPRISE: Criando banco azure_sql_demo...'
    
    -- Criar banco com configurações enterprise
    CREATE DATABASE azure_sql_demo
    ON (
        NAME = 'azure_sql_demo_data',
        FILENAME = '/var/opt/mssql/data/azure_sql_demo.mdf',
        SIZE = 100MB,
        MAXSIZE = 1GB,
        FILEGROWTH = 10MB
    )
    LOG ON (
        NAME = 'azure_sql_demo_log',
        FILENAME = '/var/opt/mssql/data/azure_sql_demo.ldf',
        SIZE = 10MB,
        MAXSIZE = 100MB,
        FILEGROWTH = 5MB
    );
    
    PRINT '✅ Banco azure_sql_demo criado com sucesso!'
    PRINT '📊 Configurações Enterprise aplicadas'
END
ELSE
BEGIN
    PRINT '✅ Banco azure_sql_demo já existe'
END
GO

-- Configurar banco para uso enterprise
USE azure_sql_demo;
GO

-- Configurações de performance enterprise
ALTER DATABASE azure_sql_demo SET RECOVERY SIMPLE;
ALTER DATABASE azure_sql_demo SET AUTO_UPDATE_STATISTICS ON;
ALTER DATABASE azure_sql_demo SET AUTO_CREATE_STATISTICS ON;
ALTER DATABASE azure_sql_demo SET AUTO_SHRINK OFF;
ALTER DATABASE azure_sql_demo SET AUTO_CLOSE OFF;

PRINT '🎯 ENTERPRISE: Banco configurado e pronto para migrações Flyway'
GO