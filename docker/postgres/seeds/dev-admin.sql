-- Seed APENAS para ambiente de desenvolvimento local (Docker Compose).
-- Não usar em produção. Senha: admin123 (bcrypt).
INSERT INTO usuarios (email, senha, nome, perfil, ativo, deve_definir_senha, criado_em, atualizado_em)
VALUES (
    'admin@vortex.com',
    '$2a$10$uVthyddDgHbLYpzdkg6uV.i.SITKooF.QgWjsjtczMKNOa620SsYi',
    'Administrador',
    'ADMIN',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
