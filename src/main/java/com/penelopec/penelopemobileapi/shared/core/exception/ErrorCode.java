package com.penelopec.penelopemobileapi.shared.core.exception;

import com.penelopec.penelopemobileapi.shared.core.result.Result;

import java.util.Map;

/**
 * Contrato base para códigos de erro.
 * Permite que diferentes domínios da aplicação criem seus próprios Enums de erro,
 * padronizando a geração de objetos DomainError.
 */
public interface ErrorCode {

  /**
   * @return O código canônico em formato String (ex: "SYS_001").
   */
  String code();

  /**
   * @return A mensagem descritiva padrão deste erro.
   */
  String defaultMessage();

  /**
   * Cria um DomainError a partir deste código de erro, usando a mensagem padrão.
   */
  default DomainError toError() {
    return DomainError.of(code(), defaultMessage());
  }

  /**
   * Cria um DomainError a partir deste código de erro com metadados adicionais.
   */
  default DomainError toError(Map<String, Object> metadata) {
    return new DomainError(code(), defaultMessage(), metadata);
  }

  /**
   * Converte o código diretamente para um Result de falha.
   * Garante a inferência automática do tipo de sucesso <T>.
   */
  default <T> Result<T, DomainError> toFailure() {
    return Result.failure(toError());
  }
}