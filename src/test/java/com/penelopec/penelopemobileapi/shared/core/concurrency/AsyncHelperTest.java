package com.penelopec.penelopemobileapi.shared.core.concurrency;

import com.penelopec.penelopemobileapi.shared.core.exception.AsyncExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do AsyncHelper")
class AsyncHelperTest {

  @Nested
  @DisplayName("Execução de Callable")
  class SupplyTests {

    @Test
    @DisplayName("Deve retornar resultado com sucesso quando a tarefa não lança erros")
    void shouldReturnResultWhenCallableIsSuccessful() {
      CompletableFuture<String> future = AsyncHelper.supply(() -> "Sucesso");

      assertThat(future).succeedsWithin(1, java.util.concurrent.TimeUnit.SECONDS)
        .isEqualTo("Sucesso");
    }

    @Test
    @DisplayName("Deve encapsular exceção quando Callable falha")
    void shouldWrapExceptionWhenCallableFails() {
      CompletableFuture<String> future = AsyncHelper.supply(() -> {
        throw new IllegalStateException("Erro forçado");
      });

      assertThatThrownBy(future::join)
        .isInstanceOf(java.util.concurrent.CompletionException.class)
        .hasCauseInstanceOf(AsyncExecutionException.class)
        .hasMessageContaining("Falha na execução assíncrona");
    }

    @Test
    @DisplayName("Deve falhar rápido se Callable for nulo")
    void shouldFailFastWhenCallableIsNull() {
      assertThatThrownBy(() -> AsyncHelper.supply(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Callable não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Desligamento do executor")
  class ShutdownTests {

    @Test
    @DisplayName("Deve permitir desligamento graceful quando não há tarefas pendentes")
    void shouldShutdownGracefullyWhenNoTasksArePending() {
      assertThatCode(AsyncHelper::shutdownNow)
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar se tentar desligar um executor já desligado")
    void shouldThrowWhenTryingToShutdownAlreadyShutdownExecutor() {
      AsyncHelper.supply(() -> null).join();
      AsyncHelper.shutdownNow();

      assertThatThrownBy(AsyncHelper::shutdownNow)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("executor já foi desligado");
    }
  }
}