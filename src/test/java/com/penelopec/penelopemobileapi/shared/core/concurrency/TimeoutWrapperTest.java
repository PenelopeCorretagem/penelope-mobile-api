package com.penelopec.penelopemobileapi.shared.core.concurrency;

import com.penelopec.penelopemobileapi.shared.core.exception.AsyncTimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do TimeoutWrapper")
class TimeoutWrapperTest {

  @Nested
  @DisplayName("Execução com Fallback")
  class ExecuteWithFallbackTests {

    @Test
    @DisplayName("shouldReturnOriginalValueWhenCompletesBeforeTimeout")
    void shouldReturnOriginalValueWhenCompletesBeforeTimeout() {
      CompletableFuture<String> future = CompletableFuture.completedFuture("Sucesso Original");

      CompletableFuture<String> result = TimeoutWrapper.executeWithFallback(
        future, Duration.ofSeconds(2), () -> "Fallback");

      assertThat(result.join()).isEqualTo("Sucesso Original");
    }

    @Test
    @DisplayName("shouldReturnFallbackWhenTimeoutExceeded")
    void shouldReturnFallbackWhenTimeoutExceeded() {
      CompletableFuture<String> delayedFuture = new CompletableFuture<>(); // Nunca completa

      CompletableFuture<String> result = TimeoutWrapper.executeWithFallback(
        delayedFuture, Duration.ofMillis(100), () -> "Valor do Fallback");

      assertThat(result.join()).isEqualTo("Valor do Fallback");
    }
  }

  @Nested
  @DisplayName("Execução Estrita (Sem Fallback)")
  class ExecuteStrictTests {

    @Test
    @DisplayName("shouldThrowExceptionWhenTimeoutExceeded")
    void shouldThrowExceptionWhenTimeoutExceeded() {
      CompletableFuture<String> delayedFuture = new CompletableFuture<>();

      CompletableFuture<String> result = TimeoutWrapper.executeStrict(delayedFuture, Duration.ofMillis(50));

      assertThatThrownBy(result::join)
        .isInstanceOf(java.util.concurrent.CompletionException.class)
        .hasCauseInstanceOf(AsyncTimeoutException.class)
        .hasMessageContaining("excedeu o tempo limite");
    }
  }

  @Nested
  @DisplayName("Combinador anyOf")
  class AnyOfTests {

    @Test
    @DisplayName("shouldReturnFirstSuccessfulResultAndCancelRemaining")
    void shouldReturnFirstSuccessfulResultAndCancelRemaining() {
      CompletableFuture<String> fastSuccess = CompletableFuture.completedFuture("primeiro sucesso");
      CompletableFuture<String> slowFuture = new CompletableFuture<>();

      CompletableFuture<String> result = TimeoutWrapper.anyOf(
        List.of(slowFuture, fastSuccess),
        Duration.ofSeconds(1));

      assertThat(result.join()).isEqualTo("primeiro sucesso");
      assertThat(slowFuture).isCancelled();
    }

    @Test
    @DisplayName("shouldFailWithAsyncTimeoutExceptionWhenGlobalTimeoutIsExceeded")
    void shouldFailWithAsyncTimeoutExceptionWhenGlobalTimeoutIsExceeded() {
      CompletableFuture<String> never1 = new CompletableFuture<>();
      CompletableFuture<String> never2 = new CompletableFuture<>();

      CompletableFuture<String> result = TimeoutWrapper.anyOf(
        List.of(never1, never2),
        Duration.ofMillis(80));

      assertThatThrownBy(result::join)
        .isInstanceOf(java.util.concurrent.CompletionException.class)
        .hasCauseInstanceOf(AsyncTimeoutException.class)
        .hasMessageContaining("Nenhuma tarefa completou com sucesso dentro de");

      assertThat(never1).isCancelled();
      assertThat(never2).isCancelled();
    }

    @Test
    @DisplayName("shouldFailWhenNoTaskCompletesSuccessfully")
    void shouldFailWhenNoTaskCompletesSuccessfully() {
      CompletableFuture<String> fail1 = CompletableFuture.failedFuture(new IllegalStateException("falha A"));
      CompletableFuture<String> fail2 = CompletableFuture.failedFuture(new IllegalArgumentException("falha B"));

      CompletableFuture<String> result = TimeoutWrapper.anyOf(
        List.of(fail1, fail2),
        Duration.ofSeconds(1));

      assertThatThrownBy(result::join)
        .isInstanceOf(java.util.concurrent.CompletionException.class)
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Nenhuma tarefa completou com sucesso");
    }

    @Test
    @DisplayName("shouldValidateInvalidInputs")
    void shouldValidateInvalidInputs() {
      assertThatThrownBy(() -> TimeoutWrapper.anyOf(null, Duration.ofSeconds(1)))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("A lista de futures não pode ser nula");

      assertThatThrownBy(() -> TimeoutWrapper.anyOf(List.of(), Duration.ofSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A lista de futures não pode ser vazia");

      assertThatThrownBy(() -> TimeoutWrapper.anyOf(List.of(CompletableFuture.completedFuture("ok")), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("O timeout global não pode ser nulo");

      assertThatThrownBy(() -> TimeoutWrapper.anyOf(List.of(CompletableFuture.completedFuture("ok")), Duration.ZERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("O timeout global deve ser maior que zero");
    }
  }
}