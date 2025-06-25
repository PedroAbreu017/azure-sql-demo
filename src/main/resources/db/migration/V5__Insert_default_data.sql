
INSERT INTO roles (name, description) VALUES 
('USER', 'Default user role with basic permissions'),
('ADMIN', 'Administrator role with full permissions'),
('MODERATOR', 'Moderator role with limited admin permissions');

-- Inserir produtos de exemplo
INSERT INTO products (name, description, price, quantity, category) VALUES 
('Laptop Dell Inspiron 15', 'Notebook Dell Inspiron 15 com Intel i7, 16GB RAM, 512GB SSD', 2999.99, 10, 'Electronics'),
('iPhone 14 Pro', 'Apple iPhone 14 Pro 128GB - Roxo Profundo', 6499.99, 5, 'Electronics'),
('Mouse Logitech MX Master 3', 'Mouse sem fio Logitech MX Master 3 para produtividade', 399.99, 25, 'Accessories'),
('Teclado Mecânico Corsair K95', 'Teclado mecânico gamer Corsair K95 RGB Platinum', 799.99, 15, 'Accessories'),
('Monitor Samsung 27"', 'Monitor Samsung 27" 4K UHD para produtividade', 1299.99, 8, 'Electronics'),
('Headset Sony WH-1000XM5', 'Headset Sony WH-1000XM5 com cancelamento de ruído', 1999.99, 12, 'Audio'),
('Webcam Logitech C920', 'Webcam Logitech C920 HD Pro para videoconferências', 299.99, 20, 'Accessories'),
('SSD Samsung 1TB', 'SSD Samsung 970 EVO Plus NVMe 1TB', 499.99, 30, 'Storage');

-- Inserir usuário administrador padrão (senha será definida pelo DataInitializer)
-- Este INSERT será ignorado se o DataInitializer já criou o usuário
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, email, password, first_name, last_name, enabled, is_active) 
    VALUES ('admin', 'admin@sistema.com', '$2a$10$dummyHashThatWillBeReplaced', 'Sistema', 'Administrador', 1, 1);
    
    -- Adicionar role de admin ao usuário admin
    INSERT INTO user_roles (user_id, role_id) 
    SELECT u.id, r.id 
    FROM users u, roles r 
    WHERE u.username = 'admin' AND r.name = 'ADMIN';
END