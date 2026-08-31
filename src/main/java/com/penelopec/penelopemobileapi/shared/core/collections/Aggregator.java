package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Agregador funcional para operações paralelas pesadas e isoladas (CPU-Bound).
 * Garante que a execução paralela não sufoque o commonPool da JVM (evitando Starvation)
 * através do provisionamento de um ForkJoinPool dedicado.
 *
 * @param <T> o tipo dos elementos
 */
public final class Aggregator<T> {

  private final Collection<T> data;

  private Aggregator(Collection<T> data) {
    this.data = data;
  }

  /**
   * Inicializa o agregador de forma fail-fast.
   */
  public static <T> Aggregator<T> of(Collection<T> data) {
    Objects.requireNonNull(data, "Aggregator: coleção não pode ser nula");
    return new Aggregator<>(data);
  }

  /**
   * Executa um mapeamento e redução em paralelo utilizando um pool isolado.
   * Ideal para processamentos analíticos pesados sem afetar o throughput da aplicação principal.
   *
   * @param parallelism Quantidade de threads a serem utilizadas no pool isolado
   * @param identity    Valor inicial da redução (deve ser neutro, ex: 0 para soma)
   * @param mapper      Função pura para transformação (livre de side-effects)
   * @param combiner    Operador binário associativo para fundir os resultados
   * @param <R>         Tipo resultante da agregação
   * @return O resultado agregado final
   */
  public <R> R parallelMapReduce(int parallelism,
                                R identity,
                                Function<? super T, ? extends R> mapper,
                                BinaryOperator<R> combiner) {
    Objects.requireNonNull(mapper, "Aggregator: mapper não pode ser nulo");
    Objects.requireNonNull(combiner, "Aggregator: combiner não pode ser nulo");

    if (parallelism <= 0) {
      throw new IllegalArgumentException("Aggregator: o paralelismo deve ser > 0");
    }

    try (ForkJoinPool customPool = new ForkJoinPool(parallelism)) {
      return customPool.submit(() ->
        data.parallelStream()
          .<R>map(mapper)
          .reduce(identity, combiner)
      ).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Execução paralela interrompida", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Falha na computação paralela", e.getCause());
    }
  }
  
  /**
   * Executa um mapeamento e redução em paralelo de forma assíncrona, utilizando um pool isolado.
   * Retorna imediatamente um CompletableFuture sem bloquear a thread chamadora.
   *
   * @param parallelism Quantidade de threads a serem utilizadas no pool isolado
   * @param identity    Valor inicial da redução (deve ser neutro, ex: 0 para soma)
   * @param mapper      Função pura para transformação (livre de side-effects)
   * @param combiner    Operador binário associativo para fundir os resultados
   * @param <R>         Tipo resultante da agregação
   * @return CompletableFuture com o resultado agregado final
   */
  public <R> CompletableFuture<R> parallelProcessAsync(int parallelism,
                                                       R identity,
                                                       Function<? super T, ? extends R> mapper,
                                                       BinaryOperator<R> combiner) {
    Objects.requireNonNull(mapper, "Aggregator: mapper não pode ser nulo");
    Objects.requireNonNull(combiner, "Aggregator: combiner não pode ser nulo");

    if (parallelism <= 0) {
      throw new IllegalArgumentException("Aggregator: o paralelismo deve ser > 0");
    }

    ForkJoinPool customPool = new ForkJoinPool(parallelism);

    return CompletableFuture
      .supplyAsync(() ->
        data.parallelStream()
          .<R>map(mapper)
          .reduce(identity, combiner), customPool)
      .whenComplete((result, error) -> customPool.shutdown());
  }
}
