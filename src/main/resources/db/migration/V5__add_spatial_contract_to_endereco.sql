ALTER TABLE endereco ADD COLUMN municipio_ibge VARCHAR(7);
ALTER TABLE endereco ADD COLUMN coordenada_origem VARCHAR(30);
ALTER TABLE endereco ADD COLUMN coordenada_precisao VARCHAR(30);
ALTER TABLE endereco ADD COLUMN coordenada_atualizada_em TIMESTAMP;

CREATE INDEX idx_endereco_municipio_ibge ON endereco(municipio_ibge);
