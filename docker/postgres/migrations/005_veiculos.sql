CREATE TABLE IF NOT EXISTS veiculos (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    marca VARCHAR(80) NOT NULL,
    modelo VARCHAR(120) NOT NULL,
    ano_fabricacao INTEGER NOT NULL,
    motor VARCHAR(30),
    combustivel VARCHAR(30),
    km_atual INTEGER,
    cliente_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_veiculos_placa UNIQUE (placa),
    CONSTRAINT fk_veiculos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);
