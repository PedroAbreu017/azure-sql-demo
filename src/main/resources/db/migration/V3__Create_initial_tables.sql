
CREATE TABLE product_audit_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT,
    action NVARCHAR(20) NOT NULL,
    old_values NVARCHAR(MAX),
    new_values NVARCHAR(MAX),
    changed_by NVARCHAR(50),
    changed_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- Criar tabela de auditoria para transações financeiras
CREATE TABLE financial_audit_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transaction_id BIGINT,
    account_id BIGINT,
    action NVARCHAR(50) NOT NULL,
    amount DECIMAL(15,2),
    balance_before DECIMAL(15,2),
    balance_after DECIMAL(15,2),
    performed_by NVARCHAR(50),
    performed_at DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    details NVARCHAR(MAX),
    FOREIGN KEY (transaction_id) REFERENCES financial_transactions(id) ON DELETE SET NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

-- Criar índices para auditoria
CREATE INDEX idx_product_audit_product_id ON product_audit_log(product_id);
CREATE INDEX idx_product_audit_date ON product_audit_log(changed_at);
CREATE INDEX idx_financial_audit_account ON financial_audit_log(account_id);
CREATE INDEX idx_financial_audit_date ON financial_audit_log(performed_at);
CREATE INDEX idx_financial_audit_transaction ON financial_audit_log(transaction_id);
