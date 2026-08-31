package com.penelopec.penelopemobileapi.shared.core.concurrency;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controla a frequência de acesso a um recurso via algoritmo Token Bucket.
 * Utiliza travas explícitas (ReentrantLock) para garantir atomicidade sob
 * altíssima concorrência sem overhead massivo.
 */
public final class RateLimiter {

  private static final long REFILL_LOCK_TIMEOUT_MILLIS = 100L;

  private final int maxTokens;
  private final long refillIntervalMillis;
  private int availableTokens;
  private long lastRefillTime;

  // Estruturas para lock striping por cliente (evita contenção entre IDs diferentes)
  private final ConcurrentHashMap<String, ReentrantLock> clientLocks = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ClientBucketState> clientStates = new ConcurrentHashMap<>();

  // Lock explícito no lugar de synchronized para controle preciso
  private final ReentrantLock lock = new ReentrantLock();

  private RateLimiter(int maxTokens, Duration refillInterval) {
    this.maxTokens = maxTokens;
    this.refillIntervalMillis = refillInterval.toMillis();
    this.availableTokens = maxTokens;
    this.lastRefillTime = System.currentTimeMillis();
  }

  /**
   * Instancia um novo limitador.
   * @param maxTokens Capacidade máxima do balde.
   * @param refillInterval Intervalo de tempo para o balde ser reabastecido.
   */
  public static RateLimiter of(int maxTokens, Duration refillInterval) {
    Objects.requireNonNull(refillInterval, "Intervalo de refil não pode ser nulo");
    if (maxTokens <= 0) {
      throw new IllegalArgumentException("Tokens máximos devem ser maiores que zero");
    }
    if (refillInterval.isNegative() || refillInterval.isZero()) {
      throw new IllegalArgumentException("Intervalo de refil deve ser maior que zero");
    }
    return new RateLimiter(maxTokens, refillInterval);
  }

  /**
   * Tenta adquirir um token imediatamente sem bloquear a Thread.
   * @return true se obteve o token, false caso o limite tenha sido atingido.
   */
  public boolean tryAcquire() {
    refill();

    if (!lock.tryLock()) {
      return false;
    }
    try {
      if (availableTokens > 0) {
        availableTokens--;
        return true;
      }
      return false;
    } finally {
      // Regra OBRIGATÓRIA no uso de Locks explícitos
      lock.unlock();
    }
  }

  /**
   * Tenta adquirir token para um cliente específico usando lock striping por ID.
   *
   * @param clientId Identificador único do cliente.
   * @return true se obteve token para o cliente, false caso contrário.
   */
  public boolean tryAcquire(String clientId) {
    Objects.requireNonNull(clientId, "clientId não pode ser nulo");

    ReentrantLock clientLock = clientLocks.computeIfAbsent(clientId, key -> new ReentrantLock());
    ClientBucketState clientState = clientStates.computeIfAbsent(
      clientId,
      key -> new ClientBucketState(maxTokens, System.currentTimeMillis())
    );

    refillClient(clientLock, clientState);

    if (!clientLock.tryLock()) {
      return false;
    }

    try {
      if (clientState.availableTokens > 0) {
        clientState.availableTokens--;
        return true;
      }
      return false;
    } finally {
      clientLock.unlock();
    }
  }

  private void refill() {
    boolean acquired = false;
    try {
      acquired = lock.tryLock(REFILL_LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
      if (!acquired) {
        return;
      }

      long now = System.currentTimeMillis();
      long elapsedTime = now - lastRefillTime;

      if (elapsedTime > refillIntervalMillis) {
        // Permite refill integral e zera a janela do tempo
        availableTokens = maxTokens;
        lastRefillTime = now;
      }
    } catch (InterruptedException e) {
      // Boilerplate obrigatório: restaura o estado de interrupção da thread
      Thread.currentThread().interrupt();
    } finally {
      if (acquired) {
        lock.unlock();
      }
    }
  }

  private void refillClient(ReentrantLock clientLock, ClientBucketState clientState) {
    boolean acquired = false;
    try {
      acquired = clientLock.tryLock(REFILL_LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
      if (!acquired) {
        return;
      }

      long now = System.currentTimeMillis();
      long elapsedTime = now - clientState.lastRefillTime;
      if (elapsedTime > refillIntervalMillis) {
        clientState.availableTokens = maxTokens;
        clientState.lastRefillTime = now;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      if (acquired) {
        clientLock.unlock();
      }
    }
  }

  private static final class ClientBucketState {
    private int availableTokens;
    private long lastRefillTime;

    private ClientBucketState(int availableTokens, long lastRefillTime) {
      this.availableTokens = availableTokens;
      this.lastRefillTime = lastRefillTime;
    }
  }
}