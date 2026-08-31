package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Pipeline funcional para processamento de coleções com Avaliação Preguiçosa (Lazy Evaluation).
 * Encapsula a Stream API adicionando proteções Fail-Fast estruturais e ignorando elementos nulos de forma transparente.
 *
 * @param <T> o tipo dos elementos no pipeline
 */
public final class CollectionPipeline<T> {

  private final Stream<T> stream;

  private CollectionPipeline(Stream<T> stream) {
    this.stream = stream;
  }

  /**
   * Inicia uma pipeline a partir de uma coleção. Elementos nulos são removidos preventivamente.
   */
  public static <T> CollectionPipeline<T> of(Collection<T> collection) {
    Objects.requireNonNull(collection, "CollectionPipeline: a coleção de origem não pode ser nula");
    return new CollectionPipeline<>(collection.stream().filter(Objects::nonNull));
  }

  /**
   * Aplica um filtro preguiçoso aos elementos da pipeline.
   */
  public CollectionPipeline<T> filter(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate, "CollectionPipeline: o predicado de filtro não pode ser nulo");
    return new CollectionPipeline<>(this.stream.filter(predicate));
  }

  /**
   * Transforma os elementos da pipeline preguiçosamente.
   */
  public <R> CollectionPipeline<R> map(Function<? super T, ? extends R> mapper) {
    Objects.requireNonNull(mapper, "CollectionPipeline: a função de mapeamento não pode ser nula");
    return new CollectionPipeline<>(this.stream.map(mapper));
  }

  /**
   * Consome os elementos da pipeline sem alterar a ordem, tipo ou valor do elemento que segue o fluxo.
   */
  public CollectionPipeline<T> peek(Consumer<? super T> consumer) {
    Objects.requireNonNull(consumer, "CollectionPipeline: a função de consumo não pode ser nula.");

    return new CollectionPipeline<>(this.stream.peek(consumer));
  }

  /**
   * Materializa a pipeline em uma lista imutável.
   */
  public List<T> toList() {
    return this.stream.toList();
  }

  /**
   * Operação de short-circuiting que avalia a pipeline e retorna o primeiro elemento processado.
   */
  public Optional<T> findFirst() {
    return this.stream.findFirst();
  }
}
