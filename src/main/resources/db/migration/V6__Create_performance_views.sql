-- View para relatório de produtos mais vendidos
CREATE VIEW vw_product_summary AS
SELECT 
    p.id,
    p.name,
    p.category,
    p.price,
    p.quantity,
    CASE 
        WHEN p.quantity > 0 THEN 'Available'
        ELSE 'Out of Stock'
    END as availability_status,
    p.created_at
FROM products p;