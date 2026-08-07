-- Itens da OS passam a ser somente peças
UPDATE ordens_servico_itens
SET tipo = 'PECA'
WHERE tipo = 'SERVICO';
