package com.penelopec.penelopemobileapi.shared.core.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Equivalence")
class EquivalenceTest {

  @Nested
  @DisplayName("AbstractEquivalence (Regras Base)")
  class AbstractEquivalenceRules {

    private final Equivalence<String> ignoreCase = Equivalences.ignoreCase();

    @Test
    @DisplayName("Deve resolver igualdade de referências e nulos automaticamente")
    void shouldHandleNullsAndSameInstancesAutomatically() {
      assertThat(ignoreCase.equivalent(null, null)).isTrue();
      assertThat(ignoreCase.equivalent("a", null)).isFalse();

      String instance = "Java";
      assertThat(ignoreCase.equivalent(instance, instance)).isTrue();
    }

    @Test
    @DisplayName("Deve delegar a comparação de negócio para a regra específica (IgnoreCase)")
    void shouldDelegateToSpecificRule() {
      assertThat(ignoreCase.equivalent("COMMONS-CORE", "commons-core")).isTrue();
      assertThat(ignoreCase.equivalent("Java", "C++")).isFalse();
    }

    @Test
    @DisplayName("Deve gerar hashes consistentes com a regra de equivalência")
    void shouldBeConsistentWithEquivalent() {
      int hash1 = ignoreCase.hash("PLATFORM");
      int hash2 = ignoreCase.hash("platform");

      assertThat(hash1).isEqualTo(hash2);
      assertThat(ignoreCase.hash(null)).isZero();
    }
  }

  @Nested
  @DisplayName("Método pairwise")
  class Pairwise {

    @Test
    @DisplayName("Deve comparar duas coleções elemento a elemento usando a regra subjacente")
    void shouldCompareCollectionsElementByElement() {
      Equivalence<Iterable<String>> pairwiseIgnoreCase = Equivalences.ignoreCase().pairwise();

      List<String> listA = List.of("A", "b", "C");
      List<String> listB = List.of("a", "B", "c");
      List<String> listC = List.of("A", "b", "X"); // Diferente
      List<String> listD = List.of("A", "b");      // Tamanho diferente

      assertThat(pairwiseIgnoreCase.equivalent(listA, listB)).isTrue();
      assertThat(pairwiseIgnoreCase.equivalent(listA, listC)).isFalse();
      assertThat(pairwiseIgnoreCase.equivalent(listA, listD)).isFalse();
    }
  }
}