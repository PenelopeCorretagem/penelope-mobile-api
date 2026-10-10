
CREATE TABLE usuario_favorito (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  anuncio_id BIGINT NOT NULL,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_usuario_favorito UNIQUE (usuario_id, anuncio_id),
  CONSTRAINT fk_usuario_favorito_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  CONSTRAINT fk_usuario_favorito_anuncio
    FOREIGN KEY (anuncio_id) REFERENCES anuncio(id)
);

CREATE INDEX idx_usuario_favorito_usuario
  ON usuario_favorito(usuario_id);
