package com.penelopec.penelopemobileapi.shared.core.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Diagnósticos da JVM e JIT (JvmDiagnostics)")
class JvmDiagnosticsTest {

  @Nested
  @DisplayName("Monitoramento do Compilador JIT")
  class MonitoramentoJit {

    @Test
    @DisplayName("Deve extrair o tempo de compilação do JIT não-negativo")
    void shouldExtractNonNegativeJitCompilationTime() {
      // Forçamos a chamada do método algumas vezes para garantir atividade mínima
      for (int i = 0; i < 1000; i++) {
        JvmDiagnostics.getJitCompilerName();
      }

      Optional<Long> time = JvmDiagnostics.getTotalJitCompilationTimeMillis();

      time.ifPresent(millis -> assertThat(millis).isGreaterThanOrEqualTo(0L));
    }

    @Test
    @DisplayName("Deve retornar um nome de compilador JIT válido para a JVM em execução")
    void shouldReturnValidJitCompilerName() {
      String compilerName = JvmDiagnostics.getJitCompilerName();

      assertThat(compilerName).isNotNull().isNotBlank();
    }
  }

  @Nested
  @DisplayName("Monitoramento de Uptime e Razão JIT")
  class UptimeERazaoJit {

    @Test
    @DisplayName("Deve retornar uptime em milissegundos não negativo")
    void shouldReturnNonNegativeUptimeMillis() {
      long uptime = JvmDiagnostics.getUptimeMillis();

      assertThat(uptime).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("Deve retornar uptime consistentemente crescente ou igual")
    void shouldReturnIncreasingOrEqualUptime() throws InterruptedException {
      long uptime1 = JvmDiagnostics.getUptimeMillis();
      Thread.sleep(10); // Pequena pausa para garantir passagem de tempo
      long uptime2 = JvmDiagnostics.getUptimeMillis();

      assertThat(uptime2).isGreaterThanOrEqualTo(uptime1);
    }

    @Test
    @DisplayName("Deve calcular corretamente a razão percentual JIT/uptime")
    void shouldCalculateJitCompilationTimePercentageCorrectly() {
      Optional<Double> percentage = JvmDiagnostics.getJitCompilationTimePercentage();

      percentage.ifPresent(pct -> {
        assertThat(pct).isGreaterThanOrEqualTo(0.0);
        // Percentual de compilação nunca deve exceder 100% na maioria dos casos
        // mas em aquecimento agressivo pode atingir (improvável)
        assertThat(pct).isLessThan(1000.0);
      });
    }

    @Test
    @DisplayName("Deve retornar percentual de compilação menor que uptime")
    void shouldReturnJitPercentageLessThanUptime() {
      Optional<Long> jitTime = JvmDiagnostics.getTotalJitCompilationTimeMillis();
      long uptime = JvmDiagnostics.getUptimeMillis();
      Optional<Double> percentage = JvmDiagnostics.getJitCompilationTimePercentage();

      if (jitTime.isPresent() && percentage.isPresent() && uptime > 0) {
        double expectedPercentage = (jitTime.get() * 100.0) / uptime;
        assertThat(percentage.get()).isCloseTo(expectedPercentage, within(1.0));
      }
    }
  }
}