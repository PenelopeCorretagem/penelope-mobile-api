package com.penelopec.penelopemobileapi.shared.core.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do RateLimiter")
class RateLimiterTest {

  @Nested
  @DisplayName("Aquisição de Tokens")
  class AcquireTests {

    @Test
    @DisplayName("Deve permitir consumo até acabar os tokens no período")
    void shouldAllowAcquireUntilEmpty() {
      RateLimiter limiter = RateLimiter.of(2, Duration.ofSeconds(10));

      assertThat(limiter.tryAcquire()).isTrue();
      assertThat(limiter.tryAcquire()).isTrue();
      assertThat(limiter.tryAcquire()).isFalse(); // 3ª tentativa deve falhar
    }

    @Test
    @DisplayName("Deve liberar acesso após o intervalo de refill")
    void shouldRefillAfterInterval() throws InterruptedException {
      RateLimiter limiter = RateLimiter.of(1, Duration.ofMillis(100));

      assertThat(limiter.tryAcquire()).isTrue();
      assertThat(limiter.tryAcquire()).isFalse();

      Thread.sleep(150); // Aguarda refill simulado

      assertThat(limiter.tryAcquire()).isTrue();
    }

    @Test
    @DisplayName("Deve isolar consumo por cliente com lock striping")
    void shouldIsolateConsumptionPerClient() {
      RateLimiter limiter = RateLimiter.of(1, Duration.ofSeconds(10));

      assertThat(limiter.tryAcquire("clienteA")).isTrue();
      assertThat(limiter.tryAcquire("clienteA")).isFalse();

      // Cliente B mantém quota independente e não é travado por A
      assertThat(limiter.tryAcquire("clienteB")).isTrue();
      assertThat(limiter.tryAcquire("clienteB")).isFalse();
    }

    @Test
    @DisplayName("Deve falhar rápido quando clientId for nulo")
    void shouldFailFastWhenClientIdIsNull() {
      RateLimiter limiter = RateLimiter.of(1, Duration.ofSeconds(1));

      assertThatThrownBy(() -> limiter.tryAcquire(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("clientId não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Falhas na Construção")
  class FailFastTests {
    @Test
    @DisplayName("Deve impedir instâncias com configurações inválidas")
    void shouldFailFast() {
      assertThatThrownBy(() -> RateLimiter.of(0, Duration.ofSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class);

      assertThatThrownBy(() -> RateLimiter.of(1, Duration.ZERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Intervalo de refil deve ser maior que zero");
    }
  }
}