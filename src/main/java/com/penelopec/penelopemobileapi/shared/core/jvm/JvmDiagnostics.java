package com.penelopec.penelopemobileapi.shared.core.jvm;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Optional;

/**
 * Utilitário de diagnóstico da JVM para métricas gerais, focando no monitoramento
 * do JIT (Just-In-Time) Compiler e status de aquecimento (Warm-up).
 */
public final class JvmDiagnostics {

  private JvmDiagnostics() {
  }

  /**
   * Retorna o tempo total acumulado em milissegundos que a JVM gastou compilando
   * bytecodes em código de máquina nativo otimizado através do JIT.
   * <p>
   * Em cenários de Warm-up (aquecimento), este valor crescerá agressivamente
   * nos primeiros minutos da aplicação e tenderá a estabilizar (platô).
   *
   * @return Tempo total de compilação em milissegundos, ou vazio se a JVM não suportar.
   */
  public static Optional<Long> getTotalJitCompilationTimeMillis() {
    CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();

    if (compilationBean != null && compilationBean.isCompilationTimeMonitoringSupported()) {
      return Optional.of(compilationBean.getTotalCompilationTime());
    }

    return Optional.empty();
  }

  /**
   * Recupera o nome do compilador JIT ativo (ex: "HotSpot 64-Bit Tiered Compilers").
   *
   * @return O nome do compilador configurado.
   */
  public static String getJitCompilerName() {
    CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
    return compilationBean != null ? compilationBean.getName() : "Unknown";
  }

  /**
   * Retorna o tempo de uptime da JVM em milissegundos desde seu início.
   * <p>
   * Útil para correlacionar métricas com o ciclo de vida da aplicação
   * e diagnosticar comportamentos de aquecimento (Warm-up).
   *
   * @return Tempo em milissegundos desde o início da JVM.
   */
  public static long getUptimeMillis() {
    RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    return runtimeBean.getUptime();
  }

  /**
   * Calcula a razão percentual entre o tempo gasto em compilação JIT
   * e o tempo total de uptime da JVM.
   * <p>
   * Exemplo: se uptime = 10000ms e jitTime = 2000ms, retorna 20.0 (20%).
   * Útil para diagnosticar se a aplicação está em fase crítica de Warm-up.
   *
   * @return Percentual de uptime gasto em compilação JIT (0.0 a 100.0+),
   *         ou vazio se o compilador JIT não tiver suporte de monitoramento.
   */
  public static Optional<Double> getJitCompilationTimePercentage() {
    Optional<Long> jitTime = getTotalJitCompilationTimeMillis();
    if (jitTime.isEmpty()) {
      return Optional.empty();
    }

    long uptime = getUptimeMillis();
    if (uptime == 0) {
      return Optional.of(0.0);
    }

    double percentage = (jitTime.get() * 100.0) / uptime;
    return Optional.of(percentage);
  }
}
