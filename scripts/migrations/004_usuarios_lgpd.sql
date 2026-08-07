-- LGPD: aceite, solicitação de exclusão e anonimização
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS lgpd_aceite_em TIMESTAMP NULL;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS lgpd_aceite_versao VARCHAR(20) NULL;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS lgpd_exclusao_solicitada_em TIMESTAMP NULL;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS lgpd_anonimizado_em TIMESTAMP NULL;
