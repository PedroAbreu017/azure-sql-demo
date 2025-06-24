-- Stored procedure para relatório de atividade de conta
CREATE PROCEDURE sp_account_activity_report
    @account_number NVARCHAR(20),
    @start_date DATETIME2 = NULL,
    @end_date DATETIME2 = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    IF @start_date IS NULL SET @start_date = DATEADD(MONTH, -1, GETDATE());
    IF @end_date IS NULL SET @end_date = GETDATE();
    
    DECLARE @account_id BIGINT;
    SELECT @account_id = id FROM accounts WHERE account_number = @account_number;
    
    IF @account_id IS NULL
    BEGIN
        SELECT 'ERROR' as status, 'Account not found' as message;
        RETURN;
    END
    
    SELECT 
        ft.id,
        ft.transaction_date,
        ft.type,
        ft.amount,
        ft.description,
        ft.status,
        ft.reference_number,
        CASE 
            WHEN ft.from_account_id = @account_id THEN 'DEBIT'
            WHEN ft.to_account_id = @account_id THEN 'CREDIT'
        END as direction
    FROM financial_transactions ft
    WHERE (ft.from_account_id = @account_id OR ft.to_account_id = @account_id)
      AND ft.transaction_date BETWEEN @start_date AND @end_date
    ORDER BY ft.transaction_date DESC;
END;
