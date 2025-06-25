-- View para relatório de transações mensais
CREATE VIEW vw_monthly_transactions AS
SELECT 
    YEAR(transaction_date) as year,
    MONTH(transaction_date) as month,
    type as transaction_type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount
FROM financial_transactions
WHERE status = 'COMPLETED'
GROUP BY YEAR(transaction_date), MONTH(transaction_date), type;