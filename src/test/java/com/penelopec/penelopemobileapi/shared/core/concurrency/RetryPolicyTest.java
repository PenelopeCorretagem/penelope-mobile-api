package com.penelopec.penelopemobileapi.shared.core.concurrency;

import com.penelopec.penelopemobileapi.shared.core.exception.RetryExhaustedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do RetryPolicy")
class RetryPolicyTest {

  @Nested
  @DisplayName("Execução com Retentativas")
  class ExecuteTests {

    @Test
    @DisplayName("Deve retornar sucesso na primeira tentativa sem delay")
    void shouldReturnSuccessOnFirstAttempt() {
      RetryPolicy policy = RetryPolicy.of(3, Duration.ofMillis(10));
      String result = policy.execute(() -> "Sucesso");

      assertThat(result).isEqualTo("Sucesso");
    }

    @Test
    @DisplayName("Deve retornar sucesso na segunda tentativa")
    void shouldReturnSuccessOnSubsequentAttempt() {
      RetryPolicy policy = RetryPolicy.of(3, Duration.ofMillis(10));

      int[] counter = {0};
      String result = policy.execute(() -> {
        if (counter[0]++ < 1) {
          throw new RuntimeException("Falha transitória");
        }
        return "Sucesso Recuperado";
      });

      assertThat(result).isEqualTo("Sucesso Recuperado");
      assertThat(counter[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve lançar RetryExhaustedException quando esgotar as tentativas")
    void shouldThrowWhenAttemptsExhausted() {
      RetryPolicy policy = RetryPolicy.of(2, Duration.ofMillis(10));

      assertThatThrownBy(() -> policy.execute(() -> {
        throw new RuntimeException("Falha Crítica");
      }))
        .isInstanceOf(RetryExhaustedException.class)
        .hasMessageContaining("Falha após 2 tentativas");
    }

    @Test
    @DisplayName("Deve aplicar backoff exponencial entre tentativas")
    void shouldApplyExponentialBackoffBetweenAttempts() {
      RetryPolicy policy = RetryPolicy.of(3, Duration.ofMillis(20));

      long startNanos = System.nanoTime();
      assertThatThrownBy(() -> policy.execute(() -> {
        throw new RuntimeException("Falha transitória");
      }))
        .isInstanceOf(RetryExhaustedException.class);
      long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

      // 3 tentativas => 2 esperas: 20ms + 40ms = 60ms (com margem para scheduler)
      assertThat(elapsedMillis).isGreaterThanOrEqualTo(45L);
    }

    @Test
    @DisplayName("Deve respeitar sequência exponencial até recuperar")
    void shouldRespectExponentialSequenceUntilSuccess() {
      RetryPolicy policy = RetryPolicy.of(4, Duration.ofMillis(15));

      int[] counter = {0};
      long startNanos = System.nanoTime();
      String result = policy.execute(() -> {
        if (counter[0]++ < 2) {
          throw new RuntimeException("Falha transitória");
        }
        return "Sucesso";
      });
      long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

      // Sucesso na 3a tentativa => esperas de 15ms e 30ms
      assertThat(result).isEqualTo("Sucesso");
      assertThat(counter[0]).isEqualTo(3);
      assertThat(elapsedMillis).isGreaterThanOrEqualTo(30L);
    }
  }
}