package com.penelopec.penelopemobileapi.shared.core.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImmutableValue")
class ImmutableValueTest {

  @Nested
  @DisplayName("Método of")
  class Of {

    @Test
    @DisplayName("Deve encapsular o valor de forma segura")
    void shouldWrapValueSafely() {
      ImmutableValue<String> wrapper = ImmutableValue.of("DomainData");

      assertThat(wrapper.get()).isEqualTo("DomainData");
    }

    @Test
    @DisplayName("Deve lançar NullPointerException quando o valor fornecido for nulo")
    void shouldFailFastWhenGivenNull() {
      assertThatThrownBy(() -> ImmutableValue.of(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Método map")
  class Map {

    @Test
    @DisplayName("Deve transformar o valor gerando nova instância sem mutar a original")
    void shouldTransformValueWithoutMutatingOriginal() {
      ImmutableValue<Integer> original = ImmutableValue.of(10);
      ImmutableValue<String> transformed = original.map(val -> "Result: " + (val * 2));

      assertThat(original.get()).isEqualTo(10);
      assertThat(transformed.get()).isEqualTo("Result: 20");
    }
  }

  @Nested
  @DisplayName("Método peek")
  class Peek {

    @Test
    @DisplayName("Deve executar a ação colateral e retornar a própria instância para encadeamento")
    void shouldExecuteActionAndReturnSameInstance() {
      ImmutableValue<String> originalWrapper = ImmutableValue.of("EventoAuditado");
      AtomicBoolean actionExecuted = new AtomicBoolean(false);

      ImmutableValue<String> returnedWrapper = originalWrapper.peek(val -> {
        assertThat(val).isEqualTo("EventoAuditado");
        actionExecuted.set(true);
      });

      assertThat(actionExecuted.get())
        .as("O consumer deve ter sido executado")
        .isTrue();

      assertThat(returnedWrapper)
        .as("O retorno deve ser estritamente a mesma referência de memória original")
        .isSameAs(originalWrapper);
    }

    @Test
    @DisplayName("Deve lançar NullPointerException quando o consumer fornecido for nulo")
    void shouldFailFastWhenConsumerIsNull() {
      ImmutableValue<String> wrapper = ImmutableValue.of("Data");

      assertThatThrownBy(() -> wrapper.peek(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nulo");
    }
  }
}