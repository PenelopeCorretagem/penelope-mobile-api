package com.penelopec.penelopemobileapi.shared.core.exception;

/**
 * Enum rica de erros padronizados da commons-core.
 * Implementa ErrorCode, encapsulando código, mensagem e a capacidade
 * de se transformar em um DomainError (Comportamento + Estado).
 */
public enum CoreErrorCode implements ErrorCode {

  INTERNAL_ERROR("CORE_500", "Ocorreu um erro interno inesperado na operação."),
  VALIDATION_FAILED("CORE_400", "Os dados fornecidos não são válidos."),
  RESOURCE_NOT_FOUND("CORE_404", "O recurso solicitado não foi encontrado.");

  private final String code;
  private final String defaultMessage;

  CoreErrorCode(String code, String defaultMessage) {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  @Override
  public String code() {
    return this.code;
  }

  @Override
  public String defaultMessage() {
    return this.defaultMessage;
  }
}
