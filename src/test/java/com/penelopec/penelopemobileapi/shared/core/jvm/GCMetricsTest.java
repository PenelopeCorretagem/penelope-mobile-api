package com.penelopec.penelopemobileapi.shared.core.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Métricas de Garbage Collector (GCMetrics)")
class GCMetricsTest {

  @Nested
  @DisplayName("Extração de Telemetria")
  class ExtracaoDeTelemetria {

    @Test
    @DisplayName("Deve retornar contagem total de coleções do GC válida e não negativa")
    void shouldReturnValidTotalCollectionCount() {
      // Emulando uma leve alocação para garantir que pelo menos um GC possa ter ocorrido
      System.gc();

      long count = GCMetrics.getTotalCollectionCount();
      assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("Deve retornar tempo acumulado de pausa do GC não negativo")
    void shouldReturnValidTotalGCPauseTime() {
      long pauseTime = GCMetrics.getTotalGCPauseTimeMillis();
      assertThat(pauseTime).isGreaterThanOrEqualTo(0L);
    }
  }

  @Nested
  @DisplayName("Validação de Estado Crítico")
  class ValidacaoCriticidade {

    @Test
    @DisplayName("Deve rejeitar thresholds negativos via Fail-Fast")
    void shouldThrowExceptionWhenThresholdIsNegative() {
      assertThatThrownBy(() -> GCMetrics.isGCPauseTimeCritical(-100L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("não pode ser negativo");
    }

    @Test
    @DisplayName("Deve avaliar criticidade corretamente baseando-se no limite fornecido")
    void shouldEvaluateCriticalityBasedOnThreshold() {
      long currentPauseTime = GCMetrics.getTotalGCPauseTimeMillis();

      // Um threshold absurdo de alto nunca deve ser crítico
      boolean isCritical = GCMetrics.isGCPauseTimeCritical(currentPauseTime + 999999L);
      assertThat(isCritical).isFalse();

      // Um threshold de 0 ou menor que o tempo atual deve acusar criticidade
      // (a menos que a JVM acabou de subir e o pauseTime seja exatamente 0)
      if (currentPauseTime > 0) {
        boolean isHighlySensitive = GCMetrics.isGCPauseTimeCritical(currentPauseTime - 1L);
        assertThat(isHighlySensitive).isTrue();
      }
    }
  }

  @Nested
  @DisplayName("Contagem por Algoritmo de GC")
  class CollectionCountPerAlgorithmTests {

    @Test
    @DisplayName("Deve retornar mapa não nulo com algorithms de GC ativos")
    void shouldReturnNonNullMapWithActiveGCAlgorithms() {
      Map<String, Long> result = GCMetrics.getCollectionCountPerAlgorithm();

      assertThat(result)
        .isNotNull()
        .isNotEmpty();
    }

    @Test
    @DisplayName("Deve retornar mapa imutável")
    void shouldReturnImmutableMap() {
      Map<String, Long> result = GCMetrics.getCollectionCountPerAlgorithm();

      assertThatThrownBy(() -> result.put("dummy", 0L))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve conter nomes de GC como chaves")
    void shouldContainGCNamesAsKeys() {
      Map<String, Long> result = GCMetrics.getCollectionCountPerAlgorithm();

      result.keySet().forEach(gcName ->
        assertThat(gcName)
          .isNotNull()
          .isNotEmpty()
          .as("Nome do GC deve estar presente")
      );
    }

    @Test
    @DisplayName("Deve retornar contagens válidas e não negativas")
    void shouldReturnValidNonNegativeCounts() {
      Map<String, Long> result = GCMetrics.getCollectionCountPerAlgorithm();

      result.values().forEach(count ->
        assertThat(count)
          .isGreaterThanOrEqualTo(0L)
          .as("Contagem deve ser não negativa")
      );
    }

    @Test
    @DisplayName("Deve manter ordem de inserção (preserva ordem de GCs)")
    void shouldMaintainInsertionOrder() {
      Map<String, Long> result1 = GCMetrics.getCollectionCountPerAlgorithm();
      Map<String, Long> result2 = GCMetrics.getCollectionCountPerAlgorithm();

      assertThat(result1.keySet()).containsExactlyElementsOf(result2.keySet());
    }
  }
}