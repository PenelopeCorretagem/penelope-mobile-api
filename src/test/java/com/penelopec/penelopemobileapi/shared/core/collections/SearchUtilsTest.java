package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SearchUtils - Operações Terminais Otimizadas")
class SearchUtilsTest {

  @Nested
  @DisplayName("Quando buscar exatamente um elemento (findExactlyOne)")
  class FindExactlyOneTests {
    @Test
    @DisplayName("Deve retornar o elemento quando apenas um corresponder (Caminho Feliz)")
    void shouldReturnElementWhenExactlyOneMatches() {
      List<String> names = List.of("Ana", "Pedro", "João");

      Optional<String> result = SearchUtils.findExactlyOne(names, n -> n.startsWith("P"));

      assertThat(result).isPresent().contains("Pedro");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando nenhum corresponder")
    void shouldReturnEmptyWhenNoneMatches() {
      List<String> names = List.of("Ana", "Pedro");

      Optional<String> result = SearchUtils.findExactlyOne(names, n -> n.equals("Maria"));

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando mais de um corresponder (Edge Case)")
    void shouldThrowExceptionWhenMultipleMatch() {
      List<String> names = List.of("Ana", "Alex", "Pedro");

      assertThatThrownBy(() -> SearchUtils.findExactlyOne(names, n -> n.startsWith("A")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("múltiplos foram encontrados");
    }
  }
  @Nested
  @DisplayName("Quando buscar o último elemento (findLast)")
  class FindLastTests {

    @Test
    @DisplayName("Deve retornar o último elemento correspondente em uma lista")
    void shouldReturnLastMatchingElementInList() {
      List<String> names = List.of("Ana", "Pedro", "Paula", "João");

      Optional<String> result = SearchUtils.findLast(names, n -> n.startsWith("P"));

      assertThat(result).isPresent().contains("Paula");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando nenhum elemento corresponder")
    void shouldReturnEmptyWhenNoneMatches() {
      List<String> names = List.of("Ana", "Pedro");

      Optional<String> result = SearchUtils.findLast(names, n -> n.startsWith("Z"));

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve respeitar a ordem de iteração em coleção que não é lista")
    void shouldRespectIterationOrderForNonListCollection() {
      LinkedHashSet<String> names = new LinkedHashSet<>(List.of("Ana", "Pedro", "Paula", "João"));

      Optional<String> result = SearchUtils.findLast(names, n -> n.startsWith("P"));

      assertThat(result).isPresent().contains("Paula");
    }

    @Test
    @DisplayName("Deve falhar rápido quando coleção ou condição forem nulas")
    void shouldThrowWhenInputsAreNull() {
      assertThatThrownBy(() -> SearchUtils.findLast(null, n -> true))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("SearchUtils: collection não pode ser nula.");

      assertThatThrownBy(() -> SearchUtils.findLast(List.of("A"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("SearchUtils: condition não pode ser nula.");
    }
  }
}