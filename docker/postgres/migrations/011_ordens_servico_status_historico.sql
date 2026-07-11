-- Histórico de transições de status da ordem de serviço.

CREATE TABLE IF NOT EXISTS ordens_servico_status_historico (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    usuario_id BIGINT,
    origem VARCHAR(30) NOT NULL DEFAULT 'SISTEMA',
    observacao VARCHAR(500),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_os_status_hist_ordem
        FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
    CONSTRAINT fk_os_status_hist_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT chk_os_status_hist_origem
        CHECK (origem IN ('CLIENTE', 'TECNICO', 'ADMIN', 'SISTEMA'))
);

CREATE INDEX IF NOT EXISTS idx_os_status_hist_ordem_id
    ON ordens_servico_status_historico (ordem_servico_id);

CREATE INDEX IF NOT EXISTS idx_os_status_hist_criado_em
    ON ordens_servico_status_historico (ordem_servico_id, criado_em DESC);
