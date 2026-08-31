package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Page")
class PageTest {

  @Nested
  @DisplayName("Quando construindo uma nova página")
  class ConstructionCases {

    @Test
    @DisplayName("Deve fazer cópia defensiva da lista original")
    void shouldMakeDefensiveCopyOfContent() {
      List<String> modifiableList = new ArrayList<>(List.of("A", "B"));
      Page<String> page = new Page<>(modifiableList, 0, 10, 2);

      modifiableList.add("C"); // Modifica a fonte original

      assertThat(page.content())
        .hasSize(2)
        .containsExactly("A", "B")
        .doesNotContain("C");
    }

    @Test
    @DisplayName("Deve falhar rápido com estado inválido de paginação")
    void shouldFailFastWithInvalidPaginationState() {
      assertThatThrownBy(() -> new Page<>(List.of(), -1, 10, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("não pode ser negativo");
    }
  }

  @Nested
  @DisplayName("Quando transformando dados via map")
  class TransformationCases {

    @Test
    @DisplayName("Deve mapear corretamente preservando metadados de paginação")
    void shouldMapContentPreservingMetadata() {
      Page<String> textPage = new Page<>(List.of("10", "20"), 1, 5, 100);

      Page<Integer> intPage = textPage.map(Integer::parseInt);

      assertThat(intPage.content()).containsExactly(10, 20);
      assertThat(intPage.pageNumber()).isEqualTo(1);
      assertThat(intPage.pageSize()).isEqualTo(5);
      assertThat(intPage.totalElements()).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("Quando filtrando dados via filter")
  class FilteringCases {

    @Test
    @DisplayName("Deve recalcular o total de elementos se estiver na primeira página (pageNumber == 0)")
    void shouldRecalculateTotalElementsWhenOnFirstPage() {
      // Inicial com 3 itens e total reportado de 10
      Page<String> page = new Page<>(List.of("A", "B", "C"), 0, 5, 10);

      // Filtramos para remover "C"
      Page<String> filteredPage = page.filter((Predicate<? super String>) item -> !item.equals("C"));

      assertThat(filteredPage.content()).containsExactly("A", "B");
      assertThat(filteredPage.pageNumber()).isEqualTo(0);
      assertThat(filteredPage.pageSize()).isEqualTo(5);

      // Foram removidos 1 item (de 3 pra 2). Total de 10 deve cair para 9.
      assertThat(filteredPage.totalElements()).isEqualTo(9);
    }

    @Test
    @DisplayName("Deve preservar o total de elementos se NÃO estiver na primeira página (pageNumber > 0)")
    void shouldKeepTotalElementsWhenNotOnFirstPage() {
      // Inicial com 3 itens e total reportado de 10, mas na página 1 (segunda aba)
      Page<String> page = new Page<>(List.of("A", "B", "C"), 1, 5, 10);

      Page<String> filteredPage = page.filter((Predicate<? super String>) item -> !item.equals("C"));

      assertThat(filteredPage.content()).containsExactly("A", "B");
      assertThat(filteredPage.pageNumber()).isEqualTo(1);
      assertThat(filteredPage.pageSize()).isEqualTo(5);

      // Total permanece intacto por instrução de domínio
      assertThat(filteredPage.totalElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("Não deve deixar o totalElements ficar negativo ao filtrar na página 0")
    void shouldNotAllowTotalElementsToBeNegative() {
      Page<String> page = new Page<>(List.of("A", "B", "C"), 0, 5, 2); // Estado simulado errôneo/inconsistente

      Page<String> filteredPage = page.filter((Predicate<? super String>) item -> false); // Remove todos

      assertThat(filteredPage.content()).isEmpty();
      assertThat(filteredPage.totalElements()).isZero();
    }
  }

  @Test
  @DisplayName("Deve integrar Page<T> com o componente Filter<T> utilizando múltiplas regras")
  void shouldIntegratePageWithDomainFilters() {
    // 1. Nossa página vinda do banco de dados (ex: 5 produtos)
    Page<BigDecimal> pricesPage = new Page<>(
      List.of(
        new BigDecimal("10"),
        new BigDecimal("-5"),
        new BigDecimal("20"),
        new BigDecimal("0")
      ),
      0,
      10,
      4
    );

    // 2. Composição de domínio altamente fluente
    // Queremos apenas valores positivos E pares
    Filter<BigDecimal> validPrices = Filter.<BigDecimal>greaterThan(BigDecimal.ZERO)
      .and(Filter.isEven());

    // 3. Aplicação da regra na coleção
    Page<BigDecimal> processedPage = pricesPage.filter(validPrices);

    // 4. Validação
    assertThat(processedPage.content())
      .containsExactly(
        new BigDecimal("10"),
        new BigDecimal("20")
      );

    assertThat(processedPage.totalElements()).isEqualTo(2); // 4 originais - 2 filtrados
  }
}