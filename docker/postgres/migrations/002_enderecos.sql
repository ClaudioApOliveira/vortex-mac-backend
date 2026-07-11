-- Aplicar em bancos existentes que ainda usam clientes.endereco (VARCHAR).

CREATE TABLE IF NOT EXISTS enderecos (
    id BIGSERIAL PRIMARY KEY,
    cep VARCHAR(9) NOT NULL,
    logradouro VARCHAR(255),
    complemento VARCHAR(255),
    numero VARCHAR(20),
    bairro VARCHAR(150),
    cidade VARCHAR(150),
    uf VARCHAR(2),
    estado VARCHAR(100),
    ibge VARCHAR(10),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS endereco_id BIGINT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'clientes' AND column_name = 'endereco'
    ) THEN
        INSERT INTO enderecos (cep, logradouro, criado_em, atualizado_em)
        SELECT '00000-000', endereco, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        FROM clientes
        WHERE endereco IS NOT NULL AND endereco <> '' AND endereco_id IS NULL;

        UPDATE clientes c
        SET endereco_id = e.id
        FROM enderecos e
        WHERE c.endereco IS NOT NULL
          AND c.endereco <> ''
          AND c.endereco_id IS NULL
          AND e.logradouro = c.endereco
          AND e.cep = '00000-000';

        ALTER TABLE clientes DROP COLUMN endereco;
    END IF;
END $$;

ALTER TABLE clientes DROP CONSTRAINT IF EXISTS uk_clientes_endereco_id;
ALTER TABLE clientes ADD CONSTRAINT uk_clientes_endereco_id UNIQUE (endereco_id);

ALTER TABLE clientes DROP CONSTRAINT IF EXISTS fk_clientes_endereco;
ALTER TABLE clientes
    ADD CONSTRAINT fk_clientes_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos (id);
