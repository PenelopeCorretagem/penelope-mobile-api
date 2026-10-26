
CREATE TABLE usuario (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  senha VARCHAR(255) NOT NULL,
  nivel_acesso VARCHAR(30) NOT NULL,
  data_nascimento DATE,
  token_redefinicao_senha VARCHAR(255),
  data_expiracao_token TIMESTAMP
);
