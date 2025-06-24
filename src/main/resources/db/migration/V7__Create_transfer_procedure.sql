-- Stored procedure para transferência de fundos
CREATE PROCEDURE sp_transfer_funds
    @from_account_number NVARCHAR(20),
    @to_account_number NVARCHAR(20),
    @amount DECIMAL(15,2),
    @description NVARCHAR(500) = NULL,
    @performed_by NVARCHAR(50) = 'SYSTEM'
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    DECLARE @from_account_id BIGINT;
    DECLARE @to_account_id BIGINT;
    DECLARE @from_balance DECIMAL(15,2);
    DECLARE @reference_number NVARCHAR(50);
    
    BEGIN TRY
        -- Validar contas de origem e destino
        SELECT @from_account_id = id, @from_balance = balance 
        FROM accounts 
        WHERE account_number = @from_account_number AND is_active = 1;
        
        SELECT @to_account_id = id 
        FROM accounts 
        WHERE account_number = @to_account_number AND is_active = 1;
        
        IF @from_account_id IS NULL
            THROW 50001, 'Source account not found or inactive', 1;
            
        IF @to_account_id IS NULL
            THROW 50002, 'Destination account not found or inactive', 1;
            
        IF @amount <= 0
            THROW 50003, 'Transfer amount must be positive', 1;
            
        IF @from_balance < @amount
            THROW 50004, 'Insufficient funds', 1;
        
        -- Gerar número de referência único
        SET @reference_number = 'TRF' + FORMAT(GETDATE(), 'yyyyMMddHHmmss') + FORMAT(ABS(CHECKSUM(NEWID())) % 10000, '0000');
        
        -- Atualizar saldos
        UPDATE accounts SET balance = balance - @amount WHERE id = @from_account_id;
        UPDATE accounts SET balance = balance + @amount WHERE id = @to_account_id;
        
        -- Registrar transação
        INSERT INTO financial_transactions (
            amount, type, description, from_account_id, to_account_id, 
            status, reference_number
        ) VALUES (
            @amount, 'TRANSFER', @description, @from_account_id, @to_account_id,
            'COMPLETED', @reference_number
        );
        
        -- Log de auditoria
        INSERT INTO financial_audit_log (
            account_id, action, amount, balance_before, balance_after, 
            performed_by, details
        ) VALUES 
        (@from_account_id, 'TRANSFER_OUT', @amount, @from_balance, @from_balance - @amount, 
         @performed_by, 'Transfer to account ' + @to_account_number),
        (@to_account_id, 'TRANSFER_IN', @amount, 
         (SELECT balance - @amount FROM accounts WHERE id = @to_account_id), 
         (SELECT balance FROM accounts WHERE id = @to_account_id),
         @performed_by, 'Transfer from account ' + @from_account_number);
        
        COMMIT TRANSACTION;
        
        SELECT 'SUCCESS' as status, @reference_number as reference_number;
        
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
