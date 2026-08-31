package com.penelopec.penelopemobileapi.shared.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CoreException")
class CoreExceptionTest {

  @Nested
  @DisplayName("Quando instanciada corretamente")
  class ValidInstantiation {

    @Test
    @DisplayName("Deve encapsular o DomainError e expor a mensagem na superclasse")
    void shouldEncapsulateDomainError() {
      DomainError error =
        DomainError.of("BUS_01", "Regra violada");

      CoreException exception =
        new BusinessException(error);

      assertThat(exception.domainError())
        .isEqualTo(error);

      assertThat(exception.getMessage())
        .isEqualTo("Regra violada");
    }
  }

  @Nested
  @DisplayName("Garantias Arquiteturais e de Performance")
  class ArchitecturalGuarantees {

    @Test
    @DisplayName("Não deve preencher a stacktrace (Zero-Cost Optimization)")
    void shouldNotFillStackTrace() {
      CoreException exception =
        new NotFoundException(
          DomainError.of(
            "NOT_FOUND",
            "Falta algo"
          )
        );

      assertThat(exception.getStackTrace())
        .isEmpty();
    }

    @Test
    @DisplayName("Deve falhar rápido (Fail-Fast) se inicializada com DomainError nulo")
    void shouldFailFastIfDomainErrorIsNull() {
      assertThatThrownBy(() ->
        new ValidationException(null))
        .isInstanceOf(
          NullPointerException.class
        )
        .hasMessageContaining(
          "DomainError não pode ser nulo"
        );
    }
  }

  @Nested
  @DisplayName("Quando consultar política de retry")
  class RetryPolicy {

    @Test
    @DisplayName("Deve retornar falso para BusinessException")
    void shouldReturnFalseForBusinessException() {
      CoreException exception =
        new BusinessException(
          DomainError.of(
            "BUS_01",
            "Erro"
          )
        );

      assertThat(exception.isRetryable())
        .isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso para NotFoundException")
    void shouldReturnFalseForNotFoundException() {
      CoreException exception =
        new NotFoundException(
          DomainError.of(
            "NF_01",
            "Não encontrado"
          )
        );

      assertThat(exception.isRetryable())
        .isFalse();
    }

    @Test
    @DisplayName("Deve permitir configuração específica por tipo de exceção")
    void shouldAllowRetryPolicyPerExceptionType() {
      CoreException exception =
        new ValidationException(
          DomainError.of(
            "VAL_01",
            "Inválido"
          )
        );

      assertThat(exception.isRetryable())
        .isTrue();
    }
  }
}