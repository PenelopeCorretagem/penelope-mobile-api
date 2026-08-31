package com.penelopec.penelopemobileapi.shared.core.object;

/**
 * Contrato base para construção segura de objetos imutáveis.
 * Qualquer Builder na commons-core deve implementar esta interface
 * para garantir um ciclo de vida padronizado de instanciação e validação.
 *
 * @param <T> O tipo do objeto a ser construído.
 */
public interface Builder<T> {
  /**
   * Constrói a instância final do objeto.
   * Deve disparar validações (Fail-Fast) e retornar uma instância imutável.
   *
   * @return A instância imutável construída.
   */
  T build();
}