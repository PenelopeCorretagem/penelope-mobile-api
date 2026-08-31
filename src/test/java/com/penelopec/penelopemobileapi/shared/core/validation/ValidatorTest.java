package com.penelopec.penelopemobileapi.shared.core.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Validator")
class ValidatorTest {

  @Nested
  @DisplayName("Quando compondo validadores com contravariância (? super T)")
  class ContravarianceCases {

    @Test
    @DisplayName("Deve permitir compor Validator<String> com Validator<Object>")
    void shouldAllowCompositionWithSuperTypeValidator() {
      // Regra genérica para qualquer objeto
      Validator<Object> notNullRule = obj -> obj != null
        ? ValidationResult.valid()
        : ValidationResult.invalid("Objeto é nulo");

      // Regra específica para String
      Validator<String> notEmptyRule = str -> !str.trim().isEmpty()
        ? ValidationResult.valid()
        : ValidationResult.invalid("String vazia");

      // O compilador só permite isso por causa do "and(Validator<? super T>)"
      Validator<String> composedValidator = notEmptyRule.and(notNullRule);

      assertThat(composedValidator.validate("Texto").isValid()).isTrue();
    }
  }

  @Nested
  @DisplayName("Quando executando validações compostas com AND")
  class AndCompositionCases {

    @Test
    @DisplayName("Deve aplicar fail-fast e retornar o primeiro erro")
    void shouldApplyFailFastOnFirstError() {
      Validator<String> failFirst = s -> ValidationResult.invalid("Erro 1");

      // Criamos um validador que lança erro se for chamado, para provar o fail-fast
      Validator<String> failSecond = s -> {
        throw new IllegalStateException("O segundo validador não deveria ser chamado!");
      };

      Validator<String> chain = failFirst.and(failSecond);
      ValidationResult result = chain.validate("teste");

      assertThat(result.isValid()).isFalse();
      assertThat(result.violations().messages()).containsExactly("Erro 1");
    }

    @Test
    @DisplayName("Deve falhar rápido caso passe validador nulo na composição")
    void shouldFailFastIfComposedWithNull() {
      Validator<String> valid = s -> ValidationResult.valid();

      assertThatThrownBy(() -> valid.and(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("validador secundário");
    }
  }

  @Nested
  @DisplayName("Quando executando validações compostas com OR")
  class OrCompositionCases {

    @Test
    @DisplayName("Deve aplicar curto-circuito de sucesso e ignorar o segundo validador")
    void shouldShortCircuitOnFirstSuccess() {
      Validator<String> passFirst = s -> ValidationResult.valid();

      // Validador que quebra a aplicação se for chamado
      Validator<String> failSecond = s -> {
        throw new IllegalStateException("O segundo validador não deveria ser chamado no cenário OR se o primeiro passou!");
      };

      Validator<String> chain = passFirst.or(failSecond);
      ValidationResult result = chain.validate("teste");

      assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Deve acionar o validador secundário caso o primeiro falhe")
    void shouldFallbackToSecondValidator() {
      Validator<String> failFirst = s -> ValidationResult.invalid("Erro Primário");
      Validator<String> passSecond = s -> ValidationResult.valid();

      Validator<String> chain = failFirst.or(passSecond);
      ValidationResult result = chain.validate("teste");

      assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar erro do validador secundário se ambos falharem")
    void shouldFailIfBothFail() {
      Validator<String> failFirst = s -> ValidationResult.invalid("Erro 1");
      Validator<String> failSecond = s -> ValidationResult.invalid("Erro 2");

      Validator<String> chain = failFirst.or(failSecond);
      ValidationResult result = chain.validate("teste");

      assertThat(result.isValid()).isFalse();
      assertThat(result.violations().messages()).containsExactly("Erro 2");
    }

    @Test
    @DisplayName("Deve falhar rápido caso passe validador nulo na composição")
    void shouldFailFastIfComposedWithNull() {
      Validator<String> valid = s -> ValidationResult.valid();

      assertThatThrownBy(() -> valid.or(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("validador secundário");
    }
  }
}