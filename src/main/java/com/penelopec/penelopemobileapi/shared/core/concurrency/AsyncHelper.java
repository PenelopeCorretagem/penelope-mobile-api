package com.penelopec.penelopemobileapi.shared.core.concurrency;

import com.penelopec.penelopemobileapi.shared.core.exception.AsyncExecutionException;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilitário stateless para execução assíncrona segura.
 * Isola a aplicação de alocações excessivas de Threads gerenciando um
 * ThreadPool otimizado com política de rejeição e backpressure.
 */
public final class AsyncHelper {

  private static final AtomicReference<ExecutorService> EXECUTOR_REFERENCE = new AtomicReference<>(newExecutor());

  private AsyncHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Executa um Callable de forma assíncrona, retornando um CompletableFuture.
   *
   * @param callable Tarefa a ser executada.
   * @param <T>      Tipo do retorno.
   * @return Future contendo o resultado da execução.
   */
  public static <T> CompletableFuture<T> supply(Callable<T> callable) {
    Objects.requireNonNull(callable, "Callable não pode ser nulo");
    return CompletableFuture.supplyAsync(() -> {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new AsyncExecutionException("Falha na execução assíncrona", e);
      }
    }, getOrCreateExecutor());
  }

  /**
   * Executa um Runnable de forma assíncrona.
   *
   * @param runnable Tarefa a ser executada.
   * @return Future para controle de conclusão.
   */
  public static CompletableFuture<Void> run(Runnable runnable) {
    Objects.requireNonNull(runnable, "Runnable não pode ser nulo");
    return CompletableFuture.runAsync(runnable, getOrCreateExecutor());
  }

  /**
   * Desliga o executor interno de forma segura e controlada.
   *
   * <p>Estratégia em duas fases:</p>
   * <ol>
   *   <li>Graceful shutdown: aguarda até 5 segundos para tarefas completarem.</li>
   *   <li>Forceful shutdown: se ainda houver tarefas, interrompe imediatamente.</li>
   * </ol>
   *
   * @throws IllegalStateException se o executor já estiver desligado ou se a thread atual for interrompida.
   */
  public static void shutdownNow() {
    ExecutorService executor = EXECUTOR_REFERENCE.getAndSet(null);

    if (executor == null || executor.isShutdown()) {
      throw new IllegalStateException("AsyncHelper: executor já foi desligado");
    }

    // Fase 1: Tentar graceful shutdown
    executor.shutdown();

    try {
      // Aguarda até 5 segundos para tarefas em execução completarem
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        // Fase 2: Se ainda houver tarefas após 5 segundos, forçar interrupção
        executor.shutdownNow();
        // Aguardar novamente após forçar, desta vez com timeout menor
        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
          throw new IllegalStateException("AsyncHelper: executor não respondeu ao shutdownNow() dentro do prazo");
        }
      }
    } catch (InterruptedException e) {
      // Se a thread de shutdown foi interrompida, restaurar o flag e forçar encerramento
      Thread.currentThread().interrupt();
      executor.shutdownNow();
      throw new IllegalStateException("AsyncHelper: thread foi interrompida durante shutdown", e);
    }
  }

  private static ExecutorService getOrCreateExecutor() {
    ExecutorService current = EXECUTOR_REFERENCE.get();
    if (current != null && !current.isShutdown()) {
      return current;
    }

    ExecutorService newExecutor = newExecutor();
    if (EXECUTOR_REFERENCE.compareAndSet(current, newExecutor)) {
      return newExecutor;
    }

    newExecutor.shutdownNow();
    return Objects.requireNonNull(EXECUTOR_REFERENCE.get(), "Executor não pôde ser inicializado.");
  }

  // ThreadPool restrito ao número de cores para evitar Context Switching excessivo,
  // com uma fila limitada e política CallerRuns para backpressure seguro.
  private static ExecutorService newExecutor() {
    return new ThreadPoolExecutor(
      Runtime.getRuntime().availableProcessors(),
      Runtime.getRuntime().availableProcessors() * 2,
      60L, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(1000),
      new ThreadPoolExecutor.CallerRunsPolicy()
    );
  }
}