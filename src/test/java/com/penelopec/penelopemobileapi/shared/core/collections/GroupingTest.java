package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Grouping - Agrupador Seguro")
class GroupingTest {

  @Nested
  @DisplayName("Quando agrupar elementos")
  class GroupingExecutionTests {

    @Test
    @DisplayName("Deve agrupar corretamente pela chave extraída (Caminho Feliz)")
    void shouldGroupByExtractedKey() {
      List<String> words = List.of("cat", "car", "dog", "ant");

      Grouping<Character, String> grouped = Grouping.of(words, word -> word.charAt(0));

      assertThat(grouped.keys()).containsExactlyInAnyOrder('c', 'd', 'a');
      assertThat(grouped.get('c')).containsExactly("cat", "car");
      assertThat(grouped.get('d')).containsExactly("dog");
    }

    @Test
    @DisplayName("Deve ignorar elementos cuja chave resolva para nulo (Edge Case)")
    void shouldIgnoreElementsWithNullKeys() {
      List<String> words = Arrays.asList("cat", null, "car");

      // Função falharia se tentasse ler length de nulo, mas o filtro interno protege
      Grouping<Integer, String> grouped = Grouping.of(words,
        w -> w.startsWith("c") ? w.length() : null);

      assertThat(grouped.get(3)).containsExactly("cat", "car");
      assertThat(grouped.keys()).doesNotContainNull();
    }

    @Test
    @DisplayName("Deve garantir imutabilidade profunda do mapa resultante")
    void shouldGuaranteeDeepImmutability() {
      List<String> words = List.of("cat");
      Grouping<Integer, String> grouped = Grouping.of(words, String::length);

      assertThatThrownBy(() -> grouped.asMap().put(4, List.of("bird")))
        .isInstanceOf(UnsupportedOperationException.class);

      assertThatThrownBy(() -> grouped.get(3).add("bat"))
        .isInstanceOf(UnsupportedOperationException.class);
    }
  }
  @Nested
  @DisplayName("Quando mapear grupos (mapGroups)")
  class MapGroupsTests {

    @Test
    @DisplayName("Deve transformar cada grupo para outro tipo mantendo as chaves")
    void shouldMapEachGroupToAnotherType() {
      List<String> words = List.of("cat", "car", "dog");

      Grouping<Character, String> grouped = Grouping.of(words, w -> w.charAt(0));
      Grouping<Character, Integer> mapped = grouped.mapGroups(
        list -> list.stream().map(String::length).toList()
      );

      assertThat(mapped.keys()).containsExactlyInAnyOrder('c', 'd');
      assertThat(mapped.get('c')).containsExactly(3, 3);
      assertThat(mapped.get('d')).containsExactly(3);
    }

    @Test
    @DisplayName("Deve manter o Grouping original intacto")
    void shouldKeepOriginalGroupingUnchanged() {
      List<String> words = List.of("cat", "car", "dog");
      Grouping<Character, String> grouped = Grouping.of(words, w -> w.charAt(0));

      Grouping<Character, String> mapped = grouped.mapGroups(
        list -> list.stream().map(String::toUpperCase).toList()
      );

      assertThat(grouped.get('c')).containsExactly("cat", "car");
      assertThat(mapped.get('c')).containsExactly("CAT", "CAR");
    }

    @Test
    @DisplayName("Deve garantir imutabilidade profunda no Grouping mapeado")
    void shouldGuaranteeDeepImmutabilityInMappedGrouping() {
      List<String> words = List.of("cat", "car");
      Grouping<Character, String> grouped = Grouping.of(words, w -> w.charAt(0));

      Grouping<Character, String> mapped = grouped.mapGroups(
        ArrayList::new
      );

      assertThatThrownBy(() -> mapped.asMap().put('x', List.of("x")))
        .isInstanceOf(UnsupportedOperationException.class);

      assertThatThrownBy(() -> mapped.get('c').add("cow"))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve falhar rápido quando mapper for nulo")
    void shouldThrowWhenMapperIsNull() {
      Grouping<Character, String> grouped = Grouping.of(List.of("cat"), w -> w.charAt(0));

      assertThatThrownBy(() -> grouped.mapGroups(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("mapeador de grupos");
    }

    @Test
    @DisplayName("Deve falhar rápido quando mapper retornar lista nula")
    void shouldThrowWhenMapperReturnsNullList() {
      Grouping<Character, String> grouped = Grouping.of(List.of("cat"), w -> w.charAt(0));

      assertThatThrownBy(() -> grouped.mapGroups(list -> null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode retornar lista nula");
    }
  }
}