package com.penelopec.penelopemobileapi.shared.core.exception;

import java.util.Objects;

/**
 * Hierarquia selada de exceções base do sistema.
 *
 * <p>Centraliza o transporte de {@link DomainError} e estabelece
 * contratos arquiteturais comuns para todas as exceções do domínio.</p>
 *
 * <p>Implementações concretas devem declarar explicitamente se o erro
 * permite retentativa através de {@link #isRetryable()}.</p>
 */
public abstract sealed class CoreException extends RuntimeException
  permits BusinessException, NotFoundException, ValidationException {

  private final DomainError domainError;

  protected CoreException(DomainError domainError) {
    super(
      Objects.requireNonNull(
        domainError,
        "DomainError não pode ser nulo"
      ).message()
    );

    this.domainError = domainError;
  }

  public DomainError domainError() {
    return domainError;
  }

  /**
   * Indica se a operação que originou esta exceção
   * pode ser executada novamente.
   *
   * <p>Retornar {@code true} não garante sucesso na retentativa;
   * apenas indica que uma nova execução é semanticamente aceitável.</p>
   *
   * @return true se o erro permitir retry; false caso contrário
   */
  public abstract boolean isRetryable();
  /**
   * OTIMIZAÇÃO ARQUITETURAL: Zero-Cost Exception.
   * Exceções de domínio são controle de fluxo, não bugs inesperados.
   * Sobrescrever este método evita que a JVM congele a thread atual
   * para percorrer o stack e montar o array de StackTraceElements.
   */
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
