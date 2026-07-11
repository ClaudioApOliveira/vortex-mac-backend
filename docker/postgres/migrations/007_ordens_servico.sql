CREATE TABLE IF NOT EXISTS ordens_servico (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    tecnico_id BIGINT NOT NULL,
    data DATE NOT NULL,
    hora TIME NOT NULL,
    km_entrada INTEGER,
    km_saida INTEGER,
    custo_servicos_terceirizados DECIMAL(12, 2) NOT NULL DEFAULT 0,
    custo_pecas DECIMAL(12, 2) NOT NULL DEFAULT 0,
    custo_mao_de_obra DECIMAL(12, 2) NOT NULL DEFAULT 0,
    preco_total DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ORCAMENTO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ordens_servico_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_ordens_servico_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id),
    CONSTRAINT fk_ordens_servico_tecnico FOREIGN KEY (tecnico_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS ordens_servico_itens (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    quantidade DECIMAL(10, 2) NOT NULL DEFAULT 1,
    valor_unitario DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tipo VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ordens_servico_itens_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE
);
