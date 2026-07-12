-- Índices para revogação em massa e limpeza de refresh tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario_revogado
    ON refresh_tokens (usuario_id, revogado);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expira_em
    ON refresh_tokens (expira_em);
