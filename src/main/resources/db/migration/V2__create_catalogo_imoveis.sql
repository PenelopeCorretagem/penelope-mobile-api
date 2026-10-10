
CREATE TABLE endereco (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  cidade VARCHAR(120) NOT NULL,
  regiao VARCHAR(120),
  uf CHAR(2) NOT NULL,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION
);

CREATE TABLE empreendimento (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(255) NOT NULL,
  descricao VARCHAR(4000) NOT NULL,
  area DOUBLE PRECISION NOT NULL,
  quantidade_quartos INTEGER NOT NULL,
  tipo VARCHAR(30) NOT NULL,
  endereco_id BIGINT NOT NULL UNIQUE,
  CONSTRAINT fk_empreendimento_endereco
    FOREIGN KEY (endereco_id) REFERENCES endereco(id)
);

CREATE TABLE anuncio (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  empreendimento_id BIGINT NOT NULL,
  preco DECIMAL(15, 2) NOT NULL,
  ativo BOOLEAN NOT NULL,
  destaque BOOLEAN NOT NULL,
  criado_em TIMESTAMP NOT NULL,
  CONSTRAINT fk_anuncio_empreendimento
    FOREIGN KEY (empreendimento_id) REFERENCES empreendimento(id)
);

CREATE TABLE tipo_midia_empreendimento (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  descricao VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE midia_empreendimento (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  empreendimento_id BIGINT NOT NULL,
  tipo_id BIGINT NOT NULL,
  url VARCHAR(2048) NOT NULL,
  CONSTRAINT fk_midia_empreendimento
    FOREIGN KEY (empreendimento_id) REFERENCES empreendimento(id),
  CONSTRAINT fk_midia_tipo
    FOREIGN KEY (tipo_id) REFERENCES tipo_midia_empreendimento(id)
);

CREATE TABLE comodidade (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  descricao VARCHAR(120) NOT NULL,
  icone VARCHAR(120)
);

CREATE TABLE empreendimento_comodidade (
  empreendimento_id BIGINT NOT NULL,
  comodidade_id BIGINT NOT NULL,
  PRIMARY KEY (empreendimento_id, comodidade_id),
  CONSTRAINT fk_empreendimento_comodidade_empreendimento
    FOREIGN KEY (empreendimento_id) REFERENCES empreendimento(id),
  CONSTRAINT fk_empreendimento_comodidade_comodidade
    FOREIGN KEY (comodidade_id) REFERENCES comodidade(id)
);

CREATE INDEX idx_anuncio_ativo ON anuncio(ativo);
CREATE INDEX idx_empreendimento_tipo ON empreendimento(tipo);
