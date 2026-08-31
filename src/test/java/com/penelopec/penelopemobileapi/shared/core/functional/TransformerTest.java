package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transformer")
class TransformerTest {

  @Nested
  @DisplayName("Quando aplicando transformações simples")
  class SimpleTransformationCases {

    @Test
    @DisplayName("Deve transformar o dado de entrada corretamente")
    void shouldTransformInput() {
      Transformer<String, Integer> stringLength = String::length;

      assertThat(stringLength.transform("Kenner")).isEqualTo(6);
    }

    @Test
    @DisplayName("Deve retornar o próprio argumento quando usar identity")
    void shouldReturnSameArgumentWithIdentity() {
      Transformer<String, String> identity = Transformer.identity();

      assertThat(identity.transform("Teste")).isEqualTo("Teste");
    }
  }

  @Nested
  @DisplayName("Quando encadeando transformações com andThen (A -> B)")
  class AndThenCases {

    @Test
    @DisplayName("Deve executar o transformer atual antes do próximo")
    void shouldExecuteCurrentBeforeNext() {
      Transformer<String, Integer> parseToInteger = Integer::parseInt;
      Transformer<Integer, Boolean> isEven = num -> num % 2 == 0;

      // Pipeline: String -> Integer -> Boolean
      Transformer<String, Boolean> isStringEven = parseToInteger.andThen(isEven);

      assertThat(isStringEven.transform("10")).isTrue();
      assertThat(isStringEven.transform("11")).isFalse();
    }

    @Test
    @DisplayName("Deve aplicar fail-fast se o próximo transformer for nulo")
    void shouldFailFastIfAfterIsNull() {
      Transformer<String, String> transformer = String::toUpperCase;

      assertThatThrownBy(() -> transformer.andThen(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("próximo passo da transformação não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Quando compondo transformações com compose (B -> A)")
  class ComposeCases {

    @Test
    @DisplayName("Deve executar o transformer anterior antes do atual")
    void shouldExecuteBeforeBeforeCurrent() {
      Transformer<Integer, Boolean> isEven = num -> num % 2 == 0;
      Transformer<String, Integer> parseToInteger = Integer::parseInt;

      // Pipeline: String -> Integer -> Boolean (lido da direita para a esquerda: f(g(x)))
      Transformer<String, Boolean> isStringEven = isEven.compose(parseToInteger);

      assertThat(isStringEven.transform("20")).isTrue();
      assertThat(isStringEven.transform("21")).isFalse();
    }

    @Test
    @DisplayName("Deve aplicar fail-fast se o transformer anterior for nulo")
    void shouldFailFastIfBeforeIsNull() {
      Transformer<String, String> transformer = String::toUpperCase;

      assertThatThrownBy(() -> transformer.compose(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("passo anterior da transformação não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Quando validando limites genéricos (PECS)")
  class PECSCases {

    @Test
    @DisplayName("Deve permitir composição segura usando covariância e contravariância")
    void shouldAllowSafeCompositionWithGenerics() {
      // Transformer que aceita Object e retorna String
      Transformer<Object, String> toString = Object::toString;

      // Transformer que aceita CharSequence e retorna Integer
      Transformer<CharSequence, Integer> length = CharSequence::length;

      // Uso do compose: O atual é 'length' (aceita CharSequence).
      // O 'before' é 'toString' (retorna String, que EXTENDS CharSequence -> Seguro!).
      // O 'before' aceita Object (que é SUPER de Integer -> Seguro!).
      Transformer<Integer, Integer> pipeline = length.compose(toString);

      // O Integer 12345 vira a String "12345" que vira o tamanho 5.
      assertThat(pipeline.transform(12345)).isEqualTo(5);
    }
  }
}