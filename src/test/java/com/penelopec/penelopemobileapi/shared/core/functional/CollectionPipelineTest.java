package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CollectionPipeline")
class CollectionPipelineTest {

  @Nested
  @DisplayName("Quando instanciado")
  class OfTests {
    @Test
    @DisplayName("Deve falhar rápido ao receber coleção nula")
    void shouldThrowExceptionWhenCollectionIsNull() {
      assertThatThrownBy(() -> CollectionPipeline.of(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nula");
    }

    @Test
    @DisplayName("Deve remover elementos nulos transparentemente")
    void shouldRemoveNullElementsWhenCreated() {
      List<String> listWithNulls = Arrays.asList("A", null, "B");
      List<String> result = CollectionPipeline.of(listWithNulls).toList();

      assertThat(result).containsExactly("A", "B");
    }
  }

  @Nested
  @DisplayName("Quando processando dados")
  class ProcessingTests {
    @Test
    @DisplayName("Deve encadear filtro e mapeamento corretamente (Caminho Feliz)")
    void shouldFilterAndMapWhenRequested() {
      List<Integer> numbers = List.of(1, 2, 3, 4, 5);

      List<String> result = CollectionPipeline.of(numbers)
        .filter(n -> n % 2 == 0)
        .map(n -> "Num: " + n)
        .toList();

      assertThat(result).containsExactly("Num: 2", "Num: 4");
    }

    @Test
    @DisplayName("Deve interromper processamento prematuramente ao usar findFirst (Short-circuit)")
    void shouldStopProcessingWhenFindFirstIsCalled() {
      List<Integer> numbers = List.of(1, 2, 3, 4, 5);

      // O pipeline é preguiçoso. Ele não avaliará 4 e 5 se o 2 satisfizer a condição.
      var firstEven = CollectionPipeline.of(numbers)
        .filter(n -> n % 2 == 0)
        .map(String::valueOf)
        .findFirst();

      assertThat(firstEven).isPresent().contains("2");
    }
  }

  @Nested
  @DisplayName("Quando usar peek")
  class PeekTests {

    @Test
    @DisplayName("Deve executar a ação sem alterar os elementos do fluxo")
    void shouldExecuteActionWithoutChangingElements() {
      List<String> observed = new java.util.ArrayList<>();

      List<String> result = CollectionPipeline.of(List.of("  A  ", "  B  "))
        .map(String::trim)
        .peek(observed::add)
        .map(String::toLowerCase)
        .toList();

      assertThat(result).containsExactly("a", "b");
      assertThat(observed).containsExactly("A", "B");
    }

    @Test
    @DisplayName("Deve manter o comportamento lazy e respeitar short-circuit")
    void shouldRespectLazyEvaluationAndShortCircuit() {
      List<Integer> observed = new java.util.ArrayList<>();

      var firstEven = CollectionPipeline.of(List.of(1, 2, 3, 4, 5))
        .filter(n -> n % 2 == 0)
        .peek(observed::add)
        .findFirst();

      assertThat(firstEven).isPresent().contains(2);
      assertThat(observed).containsExactly(2);
    }

    @Test
    @DisplayName("Deve falhar rápido quando a ação for nula")
    void shouldThrowWhenConsumerIsNull() {
      assertThatThrownBy(() -> CollectionPipeline.of(List.of("A")).peek(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("CollectionPipeline: a função de consumo não pode ser nula.");
    }
  }
}