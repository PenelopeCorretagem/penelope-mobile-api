package com.penelopec.penelopemobileapi.shared.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Aggregator - Paralelismo Isolado")
class AggregatorTest {

  @Nested
  @DisplayName("Quando processando reduções em paralelo")
  class ParallelMapReduceTests {

    @Test
    @DisplayName("Deve computar e agregar corretamente os valores em múltiplas threads (Caminho Feliz)")
    void shouldMapAndReduceCorrectlyInParallel() {
      List<Integer> largeDataset = IntStream.rangeClosed(1, 1000).boxed().collect(Collectors.toList());

      // Soma dos quadrados de 1 a 1000
      long expected = largeDataset.stream().mapToLong(i -> (long) i * i).sum();

      Long result = Aggregator.of(largeDataset)
        .parallelMapReduce(
          4,
          0L,
          num -> (long) num * num,
          Long::sum
        );

      assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve rejeitar a execução se o paralelismo for inválido (Edge Case)")
    void shouldThrowExceptionWhenParallelismIsInvalid() {
      Aggregator<String> aggregator = Aggregator.of(List.of("A"));

      assertThatThrownBy(() -> aggregator.parallelMapReduce(0, "", s -> s, String::concat))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("deve ser > 0");
    }
  }

  @Nested
@DisplayName("Quando processando reduções assíncronas em paralelo")
class ParallelProcessAsyncTests {

  @Test
  @DisplayName("Deve retornar um CompletableFuture e computar corretamente")
  void shouldReturnFutureAndComputeCorrectly() {
    List<Integer> largeDataset = IntStream.rangeClosed(1, 1000).boxed().collect(Collectors.toList());
    long expected = largeDataset.stream().mapToLong(i -> (long) i * i).sum();

    CompletableFuture<Long> future = Aggregator.of(largeDataset)
      .parallelProcessAsync(
        4,
        0L,
        num -> (long) num * num,
        Long::sum
      );

    assertThat(future).isNotNull();
    assertThat(future.join()).isEqualTo(expected);
  }

  @Test
  @DisplayName("Não deve bloquear a thread chamadora enquanto processa")
  void shouldNotBlockCallerThread() throws InterruptedException {
    List<Integer> numbers = List.of(1, 2, 3, 4, 5);
    CountDownLatch enteredMapper = new CountDownLatch(1);
    CountDownLatch releaseMapper = new CountDownLatch(1);

    CompletableFuture<Integer> future = Aggregator.of(numbers)
      .parallelProcessAsync(
        2,
        0,
        n -> {
          enteredMapper.countDown();
          try {
            releaseMapper.await(2, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Mapper interrompido", e);
          }
          return n;
        },
        Integer::sum
      );

    assertThat(enteredMapper.await(1, TimeUnit.SECONDS)).isTrue();
    assertThat(future.isDone()).isFalse();

    releaseMapper.countDown();
    assertThat(future.join()).isEqualTo(15);
  }

  @Test
  @DisplayName("Deve completar excepcionalmente quando o mapper falhar")
  void shouldCompleteExceptionallyWhenMapperFails() {
    CompletableFuture<Long> future = Aggregator.of(List.of(1, 2, 3))
      .parallelProcessAsync(
        2,
        0L,
        n -> {
          if (n == 2) throw new IllegalStateException("Falha no mapper");
          return (long) n;
        },
        Long::sum
      );

    assertThatThrownBy(future::join)
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(IllegalStateException.class)
      .hasRootCauseMessage("Falha no mapper");
  }

  @Test
  @DisplayName("Deve rejeitar execução assíncrona com paralelismo inválido")
  void shouldThrowExceptionWhenParallelismIsInvalidForAsync() {
    Aggregator<String> aggregator = Aggregator.of(List.of("A"));

    assertThatThrownBy(() -> aggregator.parallelProcessAsync(0, "", s -> s, String::concat))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("deve ser > 0");
  }

  @Test
  @DisplayName("Deve falhar rápido quando mapper ou combiner forem nulos")
  void shouldThrowWhenMapperOrCombinerIsNull() {
    Aggregator<String> aggregator = Aggregator.of(List.of("A"));

    assertThatThrownBy(() -> aggregator.parallelProcessAsync(2, "", null, String::concat))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Aggregator: mapper não pode ser nulo");

    assertThatThrownBy(() -> aggregator.parallelProcessAsync(2, "", s -> s, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Aggregator: combiner não pode ser nulo");
  }
}
}