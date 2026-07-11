CREATE TABLE IF NOT EXISTS municipios (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    uf VARCHAR(2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_municipios_uf ON municipios (uf);
