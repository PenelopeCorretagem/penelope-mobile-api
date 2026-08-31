package com.penelopec.penelopemobileapi.shared.core.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Utilitário de diagnóstico stateless para monitoramento das partições do Heap e Non-Heap da JVM.
 * <p>
 * O uso desta classe é recomendado para telemetria, health checks e lógicas de degradação graciosa,
 * abstraindo as chamadas complexas e iterativas da JMX (Java Management Extensions).
 */
public final class HeapMonitor {

  private HeapMonitor() {
  }

  /**
   * Retorna a porcentagem de uso atual do Heap total.
   * Útil para logs de saúde da aplicação ou verificações em readiness probes.
   *
   * @return um valor double representando o percentual (ex: 0.75 para 75%)
   */
  public static double getGlobalHeapUsagePercentage() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

    if (heapUsage.getMax() == -1) {
      // Caso o max não esteja definido (comportamento raro, dependente de flags),
      // usa o commited como base.
      return (double) heapUsage.getUsed() / heapUsage.getCommitted();
    }

    return (double) heapUsage.getUsed() / heapUsage.getMax();
  }

  /**
   * Inspeciona os MemoryPools da JVM para encontrar a Geração Velha (Old/Tenured Gen).
   * O estrangulamento da Old Gen é o principal indicador de que um OutOfMemoryError está iminente
   * e que a aplicação está sofrendo ou sofrerá de longas pausas de Major GC.
   *
   * @return O percentual de uso da Old Gen, se disponível na JVM atual.
   */
  public static Optional<Double> getOldGenUsagePercentage() {
    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();

    for (MemoryPoolMXBean pool : pools) {
      String name = pool.getName().toLowerCase();
      // Nomes variam por GC (ex: "G1 Old Gen", "Tenured Gen", "PS Old Gen")
      if (name.contains("old") || name.contains("tenured")) {
        MemoryUsage usage = pool.getUsage();
        double max = usage.getMax() > 0 ? usage.getMax() : usage.getCommitted();
        return Optional.of((double) usage.getUsed() / max);
      }
    }
    return Optional.empty();
  }

  /**
   * Verifica se o uso global de Heap ultrapassou o limite fornecido.
   * * @param threshold Limite de 0.0 a 1.0 (ex: 0.85 para 85%).
   * @return true se o limite foi excedido.
   */
  public static boolean isGlobalHeapCritical(double threshold) {
    if (threshold <= 0.0 || threshold > 1.0) {
      throw new IllegalArgumentException("O threshold deve ser entre 0.0 e 1.0");
    }
    return getGlobalHeapUsagePercentage() >= threshold;
  }

  /**
   * Gera um thread dump consolidado contendo o estado e stack trace completo
   * de todas as threads ativas no momento.
   *
   * <p>Útil para diagnóstico durante detecção de colapso de memória ou deadlock,
   * fornecendo visibilidade do estado geral das threads em uma única String.</p>
   *
   * @return String formatada contendo dump de todas as threads ativas.
   */
  public static String generateThreadDump() {
    Map<Thread, StackTraceElement[]> threadStacks = Thread.getAllStackTraces();
    StringJoiner dump = new StringJoiner("\n");

    dump.add("=== Thread Dump ===");
    dump.add("Timestamp: " + System.currentTimeMillis());
    dump.add("Total de threads ativas: " + threadStacks.size());
    dump.add("");

    threadStacks.forEach((thread, stackTrace) -> {
      dump.add(formatThreadInfo(thread, stackTrace));
    });

    return dump.toString();
  }

  private static String formatThreadInfo(Thread thread, StackTraceElement[] stackTrace) {
    StringJoiner threadInfo = new StringJoiner("\n");

    threadInfo.add("---");
    threadInfo.add(String.format(
      "Thread: \"%s\" (ID: %d, State: %s, Priority: %d)",
      thread.getName(), thread.getId(), thread.getState(), thread.getPriority()
    ));

    if (thread.isDaemon()) {
      threadInfo.add("  Type: Daemon");
    }

    if (thread.isInterrupted()) {
      threadInfo.add("  Status: INTERRUPTED");
    }

    if (stackTrace.length == 0) {
      threadInfo.add("  Stack trace: [vazia]");
    } else {
      threadInfo.add("  Stack trace:");
      for (StackTraceElement element : stackTrace) {
        threadInfo.add(String.format("    at %s", element));
      }
    }

    return threadInfo.toString();
  }
}