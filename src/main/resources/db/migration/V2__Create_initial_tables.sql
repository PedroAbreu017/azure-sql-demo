
CREATE TABLE accounts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_number NVARCHAR(20) NOT NULL UNIQUE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    account_type NVARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    updated_at DATETIME2,
    is_active BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criar índices para contas
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_number ON accounts(account_number);
CREATE INDEX idx_accounts_active ON accounts(is_active);

-- Criar tabela de transações financeiras
CREATE TABLE financial_transactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    type NVARCHAR(20) NOT NULL,
    description NVARCHAR(500),
    from_account_id BIGINT,
    to_account_id BIGINT,
    transaction_date DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference_number NVARCHAR(50) UNIQUE,
    FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);

-- Criar índices para transações
CREATE INDEX idx_transactions_from_account ON financial_transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON financial_transactions(to_account_id);
CREATE INDEX idx_transactions_date ON financial_transactions(transaction_date);
CREATE INDEX idx_transactions_status ON financial_transactions(status);
CREATE INDEX idx_transactions_reference ON financial_transactions(reference_number);
