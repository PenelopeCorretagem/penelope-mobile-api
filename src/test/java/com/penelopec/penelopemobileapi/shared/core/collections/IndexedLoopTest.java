package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("IndexedLoop - Iteração Otimizada")
class IndexedLoopTest {

  @Nested
  @DisplayName("Quando processar Coleções (Iterable)")
  class IterableTests {
    @Test
    @DisplayName("Deve iterar expondo o índice correto e sequencial")
    void shouldIterateWithCorrectIndex() {
      List<String> items = List.of("Zero", "Um", "Dois");
      List<String> results = new ArrayList<>();

      IndexedLoop.forEach(items, (index, item) -> results.add(index + "-" + item));

      assertThat(results).containsExactly("0-Zero", "1-Um", "2-Dois");
    }

    @Test
    @DisplayName("Deve falhar rápido se os parâmetros forem nulos")
    void shouldFailFastOnNulls() {
      assertThatThrownBy(() -> IndexedLoop.forEach((Iterable<String>) null, (i, e) -> {}))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não podem ser nulos");
    }
  }

  @Nested
  @DisplayName("Quando processar Arrays (Hot Path)")
  class ArrayTests {
    @Test
    @DisplayName("Deve aplicar laço rápido em arrays expondo o índice")
    void shouldFastLoopThroughArrays() {
      String[] items = {"A", "B"};
      List<String> results = new ArrayList<>();

      IndexedLoop.forEach(items, (index, item) -> results.add(index + "=" + item));

      assertThat(results).containsExactly("0=A", "1=B");
    }
  }

  @Nested
  @DisplayName("Quando processar em ordem reversa")
  class ReverseTests {

    @Test
    @DisplayName("Deve iterar List de trás para frente mantendo índices originais")
    void shouldIterateListReverseWithOriginalIndex() {
      List<String> items = List.of("Zero", "Um", "Dois");
      List<String> results = new ArrayList<>();

      IndexedLoop.forEachReverse(items, (index, item) -> results.add(index + "-" + item));

      assertThat(results).containsExactly("2-Dois", "1-Um", "0-Zero");
    }

    @Test
    @DisplayName("Deve iterar Array de trás para frente mantendo índices originais (Hot Path)")
    void shouldIterateArrayReverseWithOriginalIndex() {
      String[] items = {"A", "B", "C"};
      List<String> results = new ArrayList<>();

      IndexedLoop.forEachReverse(items, (index, item) -> results.add(index + "=" + item));

      assertThat(results).containsExactly("2=C", "1=B", "0=A");
    }

    @Test
    @DisplayName("Deve funcionar com coleção vazia (reversa)")
    void shouldHandleEmptyCollectionReverse() {
      List<String> empty = List.of();
      List<String> results = new ArrayList<>();

      IndexedLoop.forEachReverse(empty, (index, item) -> results.add(item));

      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar rápido em parâmetros nulos (reversa)")
    void shouldFailFastOnNullsReverse() {
      assertThatThrownBy(() -> IndexedLoop.forEachReverse((List<String>) null, (i, e) -> {}))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nula");

      assertThatThrownBy(() -> IndexedLoop.forEachReverse(List.of("A"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("consumer");

      assertThatThrownBy(() -> IndexedLoop.forEachReverse((String[]) null, (i, e) -> {}))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nulo");
    }
  }
}