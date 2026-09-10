package com.penelopec.penelopemobileapi.auth.domain;

import com.penelopec.penelopemobileapi.shared.core.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {
  INVALID_CREDENTIALS("AUTH_401", "Usuário ou senha inválidos."),
  INVALID_TOKEN("AUTH_400", "Token inválido ou não encontrado."),
  EXPIRED_RESET_TOKEN("AUTH_400", "Token expirado. Solicite um novo código."),
  TOKEN_CONFIGURATION("AUTH_500", "A configuração de autenticação está indisponível.");

  private final String code;
  private final String defaultMessage;

  AuthErrorCode(String code, String defaultMessage) {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  @Override
  public String code() {
    return code;
  }

  @Override
  public String defaultMessage() {
    return defaultMessage;
  }
}
