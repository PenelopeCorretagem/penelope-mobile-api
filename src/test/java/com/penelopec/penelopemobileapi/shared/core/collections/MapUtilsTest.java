package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapUtilsTest {

  @Nested
  @DisplayName("Testes do newHashMapWithExpectedSize")
  class NewHashMapWithExpectedSizeTests {

    @Test
    @DisplayName("Deve inicializar o mapa com sucesso para uma quantidade de itens conhecida")
    void shouldInstantiateMapWhenExpectedSizeIsProvided() {
      Map<String, String> map = MapUtils.newHashMapWithExpectedSize(100);
      assertThat(map).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Deve falhar rapidamente se o tamanho esperado for negativo")
    void shouldThrowExceptionWhenExpectedSizeIsNegative() {
      assertThatThrownBy(() -> MapUtils.newHashMapWithExpectedSize(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("O tamanho esperado não pode ser negativo.");
    }
  }

  @Nested
  @DisplayName("Testes do mergeSafe")
  class MergeSafeTests {

    @Test
    @DisplayName("Deve mesclar preservando chaves existentes no alvo")
    void shouldMergePreservingTargetKeys() {
      Map<String, String> target = new HashMap<>();
      target.put("key1", "original");

      Map<String, String> source = MapUtils.newHashMapWithExpectedSize(2);
      source.put("key1", "novo");
      source.put("key2", "novo");

      MapUtils.mergeSafe(source, target);

      assertThat(target)
        .hasSize(2)
        .containsEntry("key1", "original")
        .containsEntry("key2", "novo");
    }

    @Test
    @DisplayName("Deve ignorar de forma silenciosa se source for nulo")
    void shouldIgnoreWhenSourceIsNull() {
      Map<String, String> target = new HashMap<>();
      target.put("key1", "original");

      MapUtils.mergeSafe(null, target);

      assertThat(target).hasSize(1).containsEntry("key1", "original");
    }
  }

  @Nested
  @DisplayName("Testes do filter")
  class FilterTests {

    @Test
    @DisplayName("Deve retornar um novo mapa contendo apenas as chaves aprovadas")
    void shouldReturnNewMapContainingOnlyApprovedKeys() {
      Map<String, Integer> source = new HashMap<>();
      source.put("apple", 1);
      source.put("banana", 2);
      source.put("apricot", 3);

      Map<String, Integer> result = MapUtils.filter(source, key -> key.startsWith("a"));

      assertThat(result)
        .hasSize(2)
        .containsEntry("apple", 1)
        .containsEntry("apricot", 3)
        .doesNotContainKey("banana");
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando nenhuma chave for aprovada")
    void shouldReturnEmptyMapWhenNoKeyMatches() {
      Map<String, Integer> source = new HashMap<>();
      source.put("apple", 1);
      source.put("banana", 2);

      Map<String, Integer> result = MapUtils.filter(source, key -> false);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar um novo mapa sem alterar o mapa original")
    void shouldNotMutateSourceMap() {
      Map<String, Integer> source = new HashMap<>();
      source.put("apple", 1);
      source.put("banana", 2);

      Map<String, Integer> result = MapUtils.filter(source, key -> key.startsWith("a"));

      assertThat(source)
        .hasSize(2)
        .containsEntry("apple", 1)
        .containsEntry("banana", 2);

      assertThat(result).isNotSameAs(source);
    }

    @Test
    @DisplayName("Deve falhar rapidamente se o mapa de origem for nulo")
    void shouldThrowWhenSourceMapIsNull() {
      assertThatThrownBy(() -> MapUtils.filter(null, key -> true))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("MapUtils: o mapa de origem não pode ser nulo.");
    }

    @Test
    @DisplayName("Deve falhar rapidamente se o predicado da chave for nulo")
    void shouldThrowWhenKeyPredicateIsNull() {
      assertThatThrownBy(() -> MapUtils.filter(Map.of("a", 1), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("MapUtils: o predicado da chave não pode ser nulo.");
    }
  }
}