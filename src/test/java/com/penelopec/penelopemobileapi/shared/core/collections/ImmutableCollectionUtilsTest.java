package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableCollectionUtilsTest {

  @Nested
  @DisplayName("Testes do safeImmutableList")
  class SafeImmutableListTests {

    @Test
    @DisplayName("Deve retornar List pura filtrando entradas nulas no array e da coleção original")
    void shouldReturnCleanImmutableListWhenSourceHasNullElements() {
      List<String> dirtyList = Arrays.asList("A", null, "B");

      List<String> result = ImmutableCollectionUtils.safeImmutableList(dirtyList);

      assertThat(result).containsExactly("A", "B");
      assertThatThrownBy(() -> result.add("C"))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve retornar List vazia e segura se a origem for nula")
    void shouldReturnEmptyImmutableListWhenSourceIsNull() {
      List<String> result = ImmutableCollectionUtils.safeImmutableList(null);

      assertThat(result).isNotNull().isEmpty();
      assertThatThrownBy(() -> result.add("C"))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve garantir cópia defensiva real (alterar origem não afeta o retorno)")
    void shouldGuaranteeDefensiveCopy() {
      List<String> original = new ArrayList<>();
      original.add("Fixa");

      List<String> immutable = ImmutableCollectionUtils.safeImmutableList(original);

      original.add("Vazamento"); // Altera a origem após a cópia

      assertThat(immutable).containsExactly("Fixa").doesNotContain("Vazamento");
    }
  }

  @Nested
  @DisplayName("Testes do safeImmutableMap")
  class SafeImmutableMapTests {

    @Test
    @DisplayName("Deve retornar Map imutável filtrando chaves ou valores nulos")
    void shouldReturnImmutableMapFilteringNulls() {
      Map<String, Integer> source = new HashMap<>();
      source.put("a", 1);
      source.put(null, 2);
      source.put("c", null);
      source.put("d", 4);

      Map<String, Integer> result = ImmutableCollectionUtils.safeImmutableMap(source);

      assertThat(result)
        .hasSize(2)
        .containsEntry("a", 1)
        .containsEntry("d", 4)
        .doesNotContainKey(null);

      assertThatThrownBy(() -> result.put("x", 5))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve retornar Map vazio e imutável se a origem for nula")
    void shouldReturnEmptyImmutableMapWhenSourceIsNull() {
      Map<String, Integer> result = ImmutableCollectionUtils.safeImmutableMap(null);

      assertThat(result).isNotNull().isEmpty();
      assertThatThrownBy(() -> result.put("x", 1))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve garantir cópia defensiva real")
    void shouldGuaranteeDefensiveCopy() {
      Map<String, Integer> original = new HashMap<>();
      original.put("a", 1);

      Map<String, Integer> immutable = ImmutableCollectionUtils.safeImmutableMap(original);

      original.put("b", 2);

      assertThat(immutable).hasSize(1).containsEntry("a", 1).doesNotContainKey("b");
    }
  }
}