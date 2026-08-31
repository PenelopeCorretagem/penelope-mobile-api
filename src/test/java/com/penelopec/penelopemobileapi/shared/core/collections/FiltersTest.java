package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Filters")
class FiltersTest {

  @Nested
  @DisplayName("Garantias de Arquitetura")
  class ArchitectureCases {

    @Test
    @DisplayName("Não deve permitir instanciação da classe utilitária")
    void shouldNotAllowInstantiation() throws NoSuchMethodException {
      Constructor<Filters> constructor = Filters.class.getDeclaredConstructor();
      constructor.setAccessible(true); // Quebra o encapsulamento private para o teste

      assertThatThrownBy(constructor::newInstance)
        .isInstanceOf(InvocationTargetException.class)
        .getCause()
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("Classe utilitária não pode ser instanciada.");
    }
  }

  @Nested
  @DisplayName("Quando utilizando o filtro notNull")
  class NotNullCases {

    @Test
    @DisplayName("Deve retornar true para qualquer objeto preenchido")
    void shouldReturnTrueForNonNullObjects() {
      Filter<Object> notNull = Filters.notNull();

      assertThat(notNull.matches("Texto")).isTrue();
      assertThat(notNull.matches(100)).isTrue();
      assertThat(notNull.matches(new Object())).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para objeto nulo")
    void shouldReturnFalseForNull() {
      Filter<Object> notNull = Filters.notNull();
      assertThat(notNull.matches(null)).isFalse();
    }
  }

  @Nested
  @DisplayName("Quando utilizando o filtro isEven")
  class IsEvenCases {

    @Test
    @DisplayName("Deve retornar true para números pares em diferentes tipos (Number)")
    void shouldReturnTrueForEvenNumbers() {
      Filter<Number> isEven = Filters.isEven();

      assertThat(isEven.matches(2)).isTrue();        // Integer
      assertThat(isEven.matches(100L)).isTrue();     // Long
      assertThat(isEven.matches(4.99)).isTrue();     // Double (trunca para 4)
    }

    @Test
    @DisplayName("Deve retornar false para números ímpares")
    void shouldReturnFalseForOddNumbers() {
      Filter<Number> isEven = Filters.isEven();

      assertThat(isEven.matches(3)).isFalse();
      assertThat(isEven.matches(101L)).isFalse();
      assertThat(isEven.matches(5.0)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false de forma segura se o número for nulo")
    void shouldReturnFalseForNullNumber() {
      Filter<Number> isEven = Filters.isEven();
      assertThat(isEven.matches(null)).isFalse();
    }
  }

  @Nested
  @DisplayName("Quando utilizando o filtro isBlank")
  class IsBlankCases {

    @Test
    @DisplayName("Deve retornar true para strings vazias, nulas ou apenas com espaços")
    void shouldReturnTrueForBlankStrings() {
      Filter<CharSequence> isBlank = Filters.isBlank();

      assertThat(isBlank.matches(null)).isTrue();
      assertThat(isBlank.matches("")).isTrue();
      assertThat(isBlank.matches("   ")).isTrue();
      assertThat(isBlank.matches("\n\t")).isTrue(); // Caracteres invisíveis
    }

    @Test
    @DisplayName("Deve retornar false para textos válidos")
    void shouldReturnFalseForValidText() {
      Filter<CharSequence> isBlank = Filters.isBlank();

      assertThat(isBlank.matches("A")).isFalse();
      assertThat(isBlank.matches("  Texto  ")).isFalse(); // Tem espaços, mas tem conteúdo
      assertThat(isBlank.matches(new StringBuilder("Builder"))).isFalse(); // Testa CharSequence
    }
  }
}