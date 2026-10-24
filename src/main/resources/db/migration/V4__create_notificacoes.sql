
CREATE TABLE notificacao (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(255) NOT NULL,
  mensagem VARCHAR(4000) NOT NULL,
  criado_em TIMESTAMP NOT NULL
);

CREATE TABLE usuario_notificacao (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  notificacao_id BIGINT NOT NULL,
  lida_em TIMESTAMP,
  excluida_em TIMESTAMP,
  CONSTRAINT uq_usuario_notificacao UNIQUE (usuario_id, notificacao_id),
  CONSTRAINT fk_usuario_notificacao_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  CONSTRAINT fk_usuario_notificacao_notificacao
    FOREIGN KEY (notificacao_id) REFERENCES notificacao(id)
);

CREATE INDEX idx_usuario_notificacao_caixa_entrada
  ON usuario_notificacao(usuario_id, excluida_em, lida_em);
