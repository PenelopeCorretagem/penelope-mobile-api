package com.penelopec.penelopemobileapi.shared.core.concurrency;

import com.penelopec.penelopemobileapi.shared.core.exception.AsyncTimeoutException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Utilitário stateless para envelopar fluxos assíncronos com políticas estritas de Timeout.
 * Previne a exaustão de threads e picos de latência garantindo interrupção nativa e Fallbacks.
 */
public final class TimeoutWrapper {

  private TimeoutWrapper() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Aplica um timeout estrito a um CompletableFuture, retornando um Fallback caso o tempo expire.
   *
   * @param future   A operação assíncrona em andamento.
   * @param timeout  O limite máximo de tempo aceitável.
   * @param fallback A função de escape a ser executada caso o timeout seja atingido.
   * @param <T>      Tipo do retorno.
   * @return CompletableFuture contendo o resultado original ou o fallback.
   */
  public static <T> CompletableFuture<T> executeWithFallback(
    CompletableFuture<T> future,
    Duration timeout,
    Supplier<T> fallback) {

    Objects.requireNonNull(future, "O Future não pode ser nulo");
    Objects.requireNonNull(timeout, "O Timeout não pode ser nulo");
    Objects.requireNonNull(fallback, "O Fallback não pode ser nulo");

    return future
      .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
      .exceptionally(ex -> {
        if (isTimeout(ex)) {
          return fallback.get();
        }
        throw propagate(ex);
      });
  }

  /**
   * Aplica um timeout a um CompletableFuture. Se o tempo exceder, lança AsyncTimeoutException.
   *
   * @param future  A operação assíncrona em andamento.
   * @param timeout O limite máximo de tempo aceitável.
   * @param <T>     Tipo do retorno.
   * @return CompletableFuture blindado com timeout.
   */
  public static <T> CompletableFuture<T> executeStrict(
    CompletableFuture<T> future,
    Duration timeout) {

    Objects.requireNonNull(future, "O Future não pode ser nulo");
    Objects.requireNonNull(timeout, "O Timeout não pode ser nulo");

    return future
      .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
      .exceptionally(ex -> {
        if (isTimeout(ex)) {
          throw new AsyncTimeoutException("A operação excedeu o tempo limite de " + timeout);
        }
        throw propagate(ex);
      });
  }

  /**
   * Retorna o resultado do primeiro Future que concluir com sucesso, com timeout global estrito.
   *
   * <p>Se uma tarefa completar com sucesso, as demais são canceladas. Se o timeout global for
   * atingido antes de qualquer sucesso, todas as tarefas pendentes são canceladas e uma
   * AsyncTimeoutException é lançada.</p>
   *
   * @param futures       Lista de tarefas assíncronas concorrentes.
   * @param globalTimeout Tempo máximo total para o agrupamento.
   * @param <T>           Tipo de retorno das tarefas.
   * @return Future com o primeiro resultado bem-sucedido.
   */
  public static <T> CompletableFuture<T> anyOf(
    List<CompletableFuture<T>> futures,
    Duration globalTimeout) {

    Objects.requireNonNull(futures, "A lista de futures não pode ser nula");
    Objects.requireNonNull(globalTimeout, "O timeout global não pode ser nulo");

    if (futures.isEmpty()) {
      throw new IllegalArgumentException("A lista de futures não pode ser vazia");
    }
    if (globalTimeout.isNegative() || globalTimeout.isZero()) {
      throw new IllegalArgumentException("O timeout global deve ser maior que zero");
    }

    List<CompletableFuture<T>> safeFutures = List.copyOf(futures);
    safeFutures.forEach(future -> Objects.requireNonNull(future, "Nenhum future da lista pode ser nulo"));

    CompletableFuture<T> firstSuccess = new CompletableFuture<>();
    AtomicInteger remaining = new AtomicInteger(safeFutures.size());
    List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

    for (CompletableFuture<T> future : safeFutures) {
      future.whenComplete((value, ex) -> {
        if (ex == null) {
          if (firstSuccess.complete(value)) {
            cancelOthers(safeFutures, future);
          }
          return;
        }

        failures.add(unwrap(ex));
        if (remaining.decrementAndGet() == 0) {
          firstSuccess.completeExceptionally(buildNoSuccessException(failures));
        }
      });
    }

    return firstSuccess
      .orTimeout(globalTimeout.toMillis(), TimeUnit.MILLISECONDS)
      .exceptionally(ex -> {
        if (isTimeout(ex)) {
          cancelAll(safeFutures);
          throw new AsyncTimeoutException("Nenhuma tarefa completou com sucesso dentro de " + globalTimeout);
        }
        throw propagate(ex);
      });
  }

  private static boolean isTimeout(Throwable ex) {
    return ex instanceof TimeoutException || ex.getCause() instanceof TimeoutException;
  }

  private static Throwable unwrap(Throwable ex) {
    if (ex instanceof CompletionException ce && ce.getCause() != null) {
      return ce.getCause();
    }
    return ex;
  }

  private static void cancelOthers(List<? extends CompletableFuture<?>> futures, CompletableFuture<?> winner) {
    futures.forEach(future -> {
      if (future != winner && !future.isDone()) {
        future.cancel(true);
      }
    });
  }

  private static void cancelAll(List<? extends CompletableFuture<?>> futures) {
    futures.forEach(future -> {
      if (!future.isDone()) {
        future.cancel(true);
      }
    });
  }

  private static CompletionException buildNoSuccessException(List<Throwable> failures) {
    IllegalStateException noSuccess = new IllegalStateException("Nenhuma tarefa completou com sucesso");
    failures.forEach(noSuccess::addSuppressed);
    return new CompletionException(noSuccess);
  }

  private static CompletionException propagate(Throwable ex) {
    if (ex instanceof CompletionException ce) {
      return ce;
    }
    return new CompletionException(ex);
  }
}