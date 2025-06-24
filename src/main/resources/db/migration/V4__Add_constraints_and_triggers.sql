
ALTER TABLE accounts 
ADD CONSTRAINT chk_accounts_balance_positive 
CHECK (balance >= 0);

ALTER TABLE financial_transactions 
ADD CONSTRAINT chk_transactions_amount_positive 
CHECK (amount > 0);

ALTER TABLE products 
ADD CONSTRAINT chk_products_price_positive 
CHECK (price > 0);

ALTER TABLE products 
ADD CONSTRAINT chk_products_quantity_non_negative 
CHECK (quantity >= 0);

-- Adicionar constraint para validar tipo de conta
ALTER TABLE accounts 
ADD CONSTRAINT chk_accounts_type 
CHECK (account_type IN ('CHECKING', 'SAVINGS', 'INVESTMENT'));

-- Adicionar constraint para validar tipo de transação
ALTER TABLE financial_transactions 
ADD CONSTRAINT chk_transactions_type 
CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT'));

-- Adicionar constraint para validar status de transação
ALTER TABLE financial_transactions 
ADD CONSTRAINT chk_transactions_status 
CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'));
