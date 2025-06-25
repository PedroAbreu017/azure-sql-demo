-- Azure SQL Demo - Database Initialization
USE master;
GO

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'AzureSQLDemo')
BEGIN
    CREATE DATABASE AzureSQLDemo;
    PRINT 'Database AzureSQLDemo created successfully';
END
GO

USE AzureSQLDemo;
GO

-- Create application user
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'azure_demo_user')
BEGIN
    CREATE USER azure_demo_user WITH PASSWORD = '$(cat /run/secrets/db_password)', DEFAULT_SCHEMA = dbo;
    PRINT 'User azure_demo_user created successfully';
END
GO

-- Grant permissions
ALTER ROLE db_datareader ADD MEMBER azure_demo_user;
ALTER ROLE db_datawriter ADD MEMBER azure_demo_user;
ALTER ROLE db_ddladmin ADD MEMBER azure_demo_user;
GO

PRINT 'Database initialization completed successfully';