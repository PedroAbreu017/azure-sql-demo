-- View para relatório de contas por usuário
CREATE VIEW vw_user_accounts_summary AS
SELECT 
    u.id as user_id,
    u.username,
    u.first_name + ' ' + u.last_name as full_name,
    COUNT(a.id) as total_accounts,
    ISNULL(SUM(a.balance), 0) as total_balance,
    u.created_at as user_created_at
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id AND a.is_active = 1
WHERE u.is_active = 1
GROUP BY u.id, u.username, u.first_name, u.last_name, u.created_at;