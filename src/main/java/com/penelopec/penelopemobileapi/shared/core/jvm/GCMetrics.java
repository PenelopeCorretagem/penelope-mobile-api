package com.penelopec.penelopemobileapi.shared.core.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilitário stateless para extração de telemetria dos Garbage Collectors ativos na JVM.
 * <p>
 * O uso desta classe é recomendado para ferramentas de monitoramento interno e health checks,
 * permitindo que a aplicação detecte se está sofrendo de "GC Overhead" (passando tempo demais
 * coletando lixo e tempo de menos processando regras de negócio).
 */
public final class GCMetrics {

  private GCMetrics() {
  }

  /**
   * Calcula o número total de coletas de lixo (Minor e Major) realizadas
   * por todos os Garbage Collectors desde o início da JVM.
   *
   * @return o total de coleções efetuadas.
   */
  public static long getTotalCollectionCount() {
    return ManagementFactory.getGarbageCollectorMXBeans().stream()
      .mapToLong(GarbageCollectorMXBean::getCollectionCount)
      .filter(count -> count != -1L)
      .sum();
  }

  /**
   * Calcula o tempo acumulado (em milissegundos) que a aplicação passou em pausas
   * de Garbage Collection desde o início da JVM.
   *
   * @return o tempo total acumulado em milissegundos.
   */
  public static long getTotalGCPauseTimeMillis() {
    return ManagementFactory.getGarbageCollectorMXBeans().stream()
      .mapToLong(GarbageCollectorMXBean::getCollectionTime)
      .filter(time -> time != -1L)
      .sum();
  }

  /**
   * Verifica se o tempo acumulado de pausas do GC ultrapassa um limite crítico de tolerância.
   * Útil para acionar degradação graciosa (circuit breaking local).
   *
   * @param thresholdMillis Limite máximo aceitável de tempo pausado no histórico da JVM.
   * @return true se a JVM está ou esteve altamente engasgada.
   */
  public static boolean isGCPauseTimeCritical(long thresholdMillis) {
    if (thresholdMillis < 0) {
      throw new IllegalArgumentException("O threshold de tempo não pode ser negativo.");
    }
    return getTotalGCPauseTimeMillis() > thresholdMillis;
  }

  /**
   * Retorna um mapa imutável contendo o nome de cada algoritmo de Garbage Collector
   * ativo na JVM e sua contagem total de coletas desde o início da JVM.
   *
   * <p>Útil para diagnóstico detalhado de qual GC está trabalhando mais e monitoramento
   * individual de comportamento por algoritmo.</p>
   *
   * @return Mapa imutável com padrão de inserção: chave = nome do GC, valor = contagem de coletas.
   */
  public static Map<String, Long> getCollectionCountPerAlgorithm() {
    Map<String, Long> result = new LinkedHashMap<>();

    ManagementFactory.getGarbageCollectorMXBeans().forEach(bean -> {
      long count = bean.getCollectionCount();
      // Inclui apenas GCs com contagem válida (>= 0)
      if (count != -1L) {
        result.put(bean.getName(), count);
      }
    });

    return Map.copyOf(result);
  }
}