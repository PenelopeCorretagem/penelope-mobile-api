package com.penelopec.penelopemobileapi.shared.core.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa um erro de domínio estruturado e imutável.
 * Por quê? Padroniza o transporte de falhas de negócio entre as camadas
 * e APIs sem o alto custo de geração de stacktraces das exceções padrão do Java.
 *
 * @param code     Código canônico do erro (ex: "USER_NOT_FOUND")
 * @param message  Mensagem legível para o desenvolvedor ou log
 * @param metadata Dados adicionais de contexto do erro (imutável)
 */
public record DomainError(String code, String message, Map<String, Object> metadata) {

  /**
   * Construtor compacto: executado automaticamente durante a instanciação.
   * Aplica Fail-Fast e Defensive Copies para garantir segurança da estrutura.
   */
  public DomainError {
    Objects.requireNonNull(code, "DomainError: code não pode ser nulo");
    Objects.requireNonNull(message, "DomainError: message não pode ser nula");

    // Defensive copy para garantir imutabilidade profunda do Map
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }

  /**
   * Factory method de conveniência para erros sem metadados.
   */
  public static DomainError of(String code, String message) {
    return new DomainError(code, message, Map.of());
  }

  /**
   * Retorna uma nova instância de {@code DomainError} contendo todos os metadados
   * atuais acrescidos do novo par chave-valor informado.
   *
   * <p>Este método preserva imutabilidade absoluta: a instância atual não é
   * modificada. Em vez disso, uma nova instância é criada contendo uma cópia
   * imutável do metadata atualizado.</p>
   *
   * <p>Se a chave já existir, seu valor será substituído na nova instância.</p>
   *
   * @param key chave do metadado a ser adicionada
   * @param value valor associado à chave
   * @return nova instância de {@code DomainError} contendo o metadata atualizado
   * @throws NullPointerException se {@code key} for nulo
   */
  public DomainError withMetadata(String key, Object value) {
    Objects.requireNonNull(key, "key não pode ser nulo");

    Map<String, Object> newMetadata = new HashMap<>(metadata);
    newMetadata.put(key, value);

    return new DomainError(
      code,
      message,
      Map.copyOf(newMetadata)
    );
  }
}
