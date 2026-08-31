package com.penelopec.penelopemobileapi.shared.core.concurrency;

import com.penelopec.penelopemobileapi.shared.core.exception.RetryExhaustedException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Utilitário stateless para execução de blocos de código com política de retentativa.
 * Em arquiteturas Java 21+, o uso de Thread.sleep interno atua de forma amigável
 * às Virtual Threads, não bloqueando threads do Sistema Operacional.
 */
public final class RetryPolicy {

  private final int maxAttempts;
  private final Duration initialDelay;

  private RetryPolicy(int maxAttempts, Duration initialDelay) {
    this.maxAttempts = maxAttempts;
    this.initialDelay = initialDelay;
  }

  /**
   * Define a política de retentativas.
   * @param maxAttempts Número máximo de tentativas (deve ser >= 1).
    * @param delay Tempo base de espera entre as tentativas.
   */
  public static RetryPolicy of(int maxAttempts, Duration delay) {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("O máximo de tentativas deve ser pelo menos 1");
    }
    Objects.requireNonNull(delay, "O delay não pode ser nulo");
    return new RetryPolicy(maxAttempts, delay);
  }

  /**
    * Executa o callable fornecido com retentativa e backoff exponencial.
    * A espera segue a progressão: delay, delay*2, delay*4, ...
    * até o limite de tentativas.
    *
   * @param action Ação a ser executada.
   * @param <T> Tipo de retorno.
   * @return O resultado da ação.
   */
  public <T> T execute(Callable<T> action) {
    Objects.requireNonNull(action, "A ação não pode ser nula");

    int attempt = 0;
    while (true) {
      attempt++;
      try {
        return action.call();
      } catch (Exception ex) {
        if (attempt >= maxAttempts) {
          throw new RetryExhaustedException("Falha após " + maxAttempts + " tentativas", ex);
        }
        sleep(attempt);
      }
    }
  }

  private void sleep(int attempt) {
    try {
      Thread.sleep(backoffDelayMillis(attempt));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("A retentativa foi interrompida", e);
    }
  }

  private long backoffDelayMillis(int attempt) {
    long baseDelayMillis = initialDelay.toMillis();
    long multiplier = 1L;

    for (int i = 1; i < attempt; i++) {
      if (multiplier > Long.MAX_VALUE / 2L) {
        multiplier = Long.MAX_VALUE;
        break;
      }
      multiplier *= 2L;
    }

    if (baseDelayMillis > 0 && multiplier > Long.MAX_VALUE / baseDelayMillis) {
      return Long.MAX_VALUE;
    }
    return baseDelayMillis * multiplier;
  }
}