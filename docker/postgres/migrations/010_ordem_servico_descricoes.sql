ALTER TABLE ordens_servico
    ADD COLUMN IF NOT EXISTS descricao_servicos_terceirizados VARCHAR(500);

ALTER TABLE ordens_servico
    ADD COLUMN IF NOT EXISTS descricao_mao_de_obra VARCHAR(500);
