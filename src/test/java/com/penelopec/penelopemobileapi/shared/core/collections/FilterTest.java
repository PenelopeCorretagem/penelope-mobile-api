package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Filter")
class FilterTest {

  @Nested
  @DisplayName("Quando operando com filtros de comparação (Single Bound)")
  class ComparableBoundsCases {

    @Test
    @DisplayName("Deve filtrar corretamente números maiores que o limite")
    void shouldFilterNumbersGreaterThanThreshold() {
      Filter<BigDecimal> isPositive = Filter.greaterThan(BigDecimal.ZERO);

      assertThat(isPositive.matches(BigDecimal.TEN)).isTrue();
      assertThat(isPositive.matches(new BigDecimal("-5"))).isFalse();
      assertThat(isPositive.matches(null)).isFalse();
    }

    @Test
    @DisplayName("Deve aplicar fail-fast se threshold for nulo")
    void shouldFailFastWhenThresholdIsNull() {
      assertThatThrownBy(() -> Filter.greaterThan(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("limite (threshold) não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Quando operando com filtros complexos (Multiple Bounds)")
  class MultipleBoundsCases {

    @Test
    @DisplayName("Deve avaliar regras de CharSequence e Comparable simultaneamente")
    void shouldEvaluateMultipleInterfacesSafely() {
      // String atende a ambos: CharSequence e Comparable
      Filter<String> complexFilter = Filter.textGreaterThan(3, "B");

      assertThat(complexFilter.matches("CARLOS")).isTrue(); // Length >= 3, "CARLOS" > "B"
      assertThat(complexFilter.matches("A")).isFalse();      // Falso para os dois
      assertThat(complexFilter.matches("B")).isFalse();      // Falso para limite (compareTo é == 0)
    }
  }

  @Nested
  @DisplayName("Quando operando com filtros numéricos abstratos (Number Bounds)")
  class NumberBoundsCases {

    @Test
    @DisplayName("Deve identificar corretamente números pares de diferentes tipos")
    void shouldIdentifyEvenNumbersAcrossNumberTypes() {
      Filter<Number> isEven = Filter.isEven();

      // Integer
      assertThat(isEven.matches(2)).isTrue();
      assertThat(isEven.matches(3)).isFalse();

      // Long
      assertThat(isEven.matches(100L)).isTrue();
      assertThat(isEven.matches(101L)).isFalse();

      // Double (Truncamento via longValue: 4.9 vira 4, que é par)
      assertThat(isEven.matches(4.9)).isTrue();
      assertThat(isEven.matches(5.1)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso se o número for nulo")
    void shouldReturnFalseIfNumberIsNull() {
      Filter<Number> isEven = Filter.isEven();
      assertThat(isEven.matches(null)).isFalse();
    }
  }
}