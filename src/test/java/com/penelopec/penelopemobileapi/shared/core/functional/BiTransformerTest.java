package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BiTransformer")
class BiTransformerTest {

  @Nested
  @DisplayName("Quando transformar múltiplos dados")
  class Transform {

    @Test
    @DisplayName("Deve aplicar a transformação corretamente usando dois argumentos")
    void shouldTransformWhenInputsAreValid() {
      BiTransformer<String, String, String> concat = (prefix, text) -> prefix + ": " + text;

      String result = concat.transform("INFO", "Sistema iniciado");

      assertThat(result).isEqualTo("INFO: Sistema iniciado");
    }
  }

  @Nested
  @DisplayName("Quando compor transformações com andThen")
  class AndThen {

    @Test
    @DisplayName("Deve encadear o resultado do BiTransformer para um Transformer de único parâmetro")
    void shouldChainResultToSingleTransformer() {
      BiTransformer<Integer, Integer, Integer> sum = Integer::sum;
      Transformer<Integer, String> format = result -> "Total: " + result;

      BiTransformer<Integer, Integer, String> composed = sum.andThen(format);

      assertThat(composed.transform(10, 5)).isEqualTo("Total: 15");
    }

    @Test
    @DisplayName("Deve falhar rápido se o próximo transformer for nulo")
    void shouldThrowExceptionWhenNextTransformerIsNull() {
      BiTransformer<String, String, String> concat = (a, b) -> a + b;

      assertThatThrownBy(() -> concat.andThen(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("BiTransformer: o próximo passo não pode ser nulo.");
    }
  }
}