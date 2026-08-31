package com.penelopec.penelopemobileapi.shared.core.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Monitoramento de Heap (HeapMonitor)")
class HeapMonitorTest {

  @Nested
  @DisplayName("Cálculo de Percentuais")
  class CalculoPercentuais {

    @Test
    @DisplayName("Deve retornar percentual de uso do Heap global em limite válido")
    void shouldReturnValidPercentageWhenQueryingGlobalHeap() {
      double usage = HeapMonitor.getGlobalHeapUsagePercentage();

      assertThat(usage).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Deve capturar o uso da Old Gen se a JVM atual expor a métrica")
    void shouldReturnOldGenPercentageWhenAvailable() {
      Optional<Double> oldGenUsage = HeapMonitor.getOldGenUsagePercentage();

      oldGenUsage.ifPresent(usage -> assertThat(usage).isBetween(0.0, 1.0));
    }
  }

  @Nested
  @DisplayName("Validação de Estado Crítico")
  class ValidacaoCriticidade {

    @Test
    @DisplayName("Deve acusar criticidade apenas se o threshold for excedido")
    void shouldReturnTrueOnlyWhenThresholdIsExceeded() {
      double currentUsage = HeapMonitor.getGlobalHeapUsagePercentage();
      double relaxedThreshold = Math.min(1.0, currentUsage + 0.01d);
      double sensitiveThreshold = Math.max(0.0001d, currentUsage - 0.01d);

      boolean isCritical = HeapMonitor.isGlobalHeapCritical(relaxedThreshold);
      assertThat(isCritical).isFalse();

      boolean isHighlySensitive = HeapMonitor.isGlobalHeapCritical(sensitiveThreshold);
      assertThat(isHighlySensitive).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar limites de threshold inválidos via Fail-Fast")
    void shouldThrowExceptionWhenThresholdIsInvalid() {
      assertThatThrownBy(() -> HeapMonitor.isGlobalHeapCritical(-0.1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("threshold deve ser");

      assertThatThrownBy(() -> HeapMonitor.isGlobalHeapCritical(1.5))
        .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("Geração de Thread Dump")
  class GenerateThreadDumpTests {

    @Test
    @DisplayName("Deve gerar dump com informações de todas as threads ativas")
    void shouldGenerateThreadDumpWithAllActiveThreads() {
      String dump = HeapMonitor.generateThreadDump();

      assertThat(dump)
        .contains("=== Thread Dump ===")
        .contains("Timestamp:")
        .contains("Total de threads ativas:")
        .contains("Thread:")
        .contains("Stack trace:");
    }

    @Test
    @DisplayName("Deve incluir informações essenciais de cada thread (ID, nome, estado)")
    void shouldIncludeThreadEssentialInfo() {
      String dump = HeapMonitor.generateThreadDump();

      assertThat(dump)
        .contains("ID:") // Thread ID
        .contains("State:") // Thread state
        .contains("Priority:"); // Priority
    }

    @Test
    @DisplayName("Deve conter stack trace de pelo menos a thread atual")
    void shouldContainStackTraceOfCurrentThread() {
      String threadName = Thread.currentThread().getName();
      String dump = HeapMonitor.generateThreadDump();

      assertThat(dump)
        .contains(threadName)
        .contains("at "); // Stack trace format
    }

    @Test
    @DisplayName("Deve marcar threads daemon quando aplicável")
    void shouldMarkDaemonThreads() {
      Thread daemonThread = new Thread(() -> {}, "TestDaemon");
      daemonThread.setDaemon(true);
      daemonThread.start();

      try {
        String dump = HeapMonitor.generateThreadDump();
        assertThat(dump).contains("Type: Daemon");
      } finally {
        daemonThread.interrupt();
      }
    }
  }
}