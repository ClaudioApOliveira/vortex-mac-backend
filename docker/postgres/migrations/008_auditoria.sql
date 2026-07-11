ALTER TABLE enderecos
    ADD COLUMN IF NOT EXISTS usuario_inclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS usuario_alteracao_id BIGINT;

ALTER TABLE enderecos DROP CONSTRAINT IF EXISTS fk_enderecos_usuario_inclusao;
ALTER TABLE enderecos DROP CONSTRAINT IF EXISTS fk_enderecos_usuario_alteracao;
ALTER TABLE enderecos
    ADD CONSTRAINT fk_enderecos_usuario_inclusao
        FOREIGN KEY (usuario_inclusao_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_enderecos_usuario_alteracao
        FOREIGN KEY (usuario_alteracao_id) REFERENCES usuarios (id);

ALTER TABLE clientes
    ADD COLUMN IF NOT EXISTS usuario_inclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS usuario_alteracao_id BIGINT;

ALTER TABLE clientes DROP CONSTRAINT IF EXISTS fk_clientes_usuario_inclusao;
ALTER TABLE clientes DROP CONSTRAINT IF EXISTS fk_clientes_usuario_alteracao;
ALTER TABLE clientes
    ADD CONSTRAINT fk_clientes_usuario_inclusao
        FOREIGN KEY (usuario_inclusao_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_clientes_usuario_alteracao
        FOREIGN KEY (usuario_alteracao_id) REFERENCES usuarios (id);

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS usuario_inclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS usuario_alteracao_id BIGINT;

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS fk_usuarios_usuario_inclusao;
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS fk_usuarios_usuario_alteracao;
ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_usuario_inclusao
        FOREIGN KEY (usuario_inclusao_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_usuarios_usuario_alteracao
        FOREIGN KEY (usuario_alteracao_id) REFERENCES usuarios (id);

ALTER TABLE veiculos
    ADD COLUMN IF NOT EXISTS usuario_inclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS usuario_alteracao_id BIGINT;

ALTER TABLE veiculos DROP CONSTRAINT IF EXISTS fk_veiculos_usuario_inclusao;
ALTER TABLE veiculos DROP CONSTRAINT IF EXISTS fk_veiculos_usuario_alteracao;
ALTER TABLE veiculos
    ADD CONSTRAINT fk_veiculos_usuario_inclusao
        FOREIGN KEY (usuario_inclusao_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_veiculos_usuario_alteracao
        FOREIGN KEY (usuario_alteracao_id) REFERENCES usuarios (id);

ALTER TABLE ordens_servico
    ADD COLUMN IF NOT EXISTS usuario_inclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS usuario_alteracao_id BIGINT;

ALTER TABLE ordens_servico DROP CONSTRAINT IF EXISTS fk_ordens_servico_usuario_inclusao;
ALTER TABLE ordens_servico DROP CONSTRAINT IF EXISTS fk_ordens_servico_usuario_alteracao;
ALTER TABLE ordens_servico
    ADD CONSTRAINT fk_ordens_servico_usuario_inclusao
        FOREIGN KEY (usuario_inclusao_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_ordens_servico_usuario_alteracao
        FOREIGN KEY (usuario_alteracao_id) REFERENCES usuarios (id);

ALTER TABLE ordens_servico_itens
    ADD COLUMN IF NOT EXISTS usuario_inclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS usuario_alteracao_id BIGINT;

ALTER TABLE ordens_servico_itens DROP CONSTRAINT IF EXISTS fk_ordens_servico_itens_usuario_inclusao;
ALTER TABLE ordens_servico_itens DROP CONSTRAINT IF EXISTS fk_ordens_servico_itens_usuario_alteracao;
ALTER TABLE ordens_servico_itens
    ADD CONSTRAINT fk_ordens_servico_itens_usuario_inclusao
        FOREIGN KEY (usuario_inclusao_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_ordens_servico_itens_usuario_alteracao
        FOREIGN KEY (usuario_alteracao_id) REFERENCES usuarios (id);
