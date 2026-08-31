package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Utilitário de alta performance para iterações.
 * Preenche a lacuna da Stream API nativa fornecendo iteração com índice sem overhead
 * de Autoboxing excessivo ou geração de pipelines encadeados.
 */
public final class IndexedLoop {

  private IndexedLoop() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Itera sobre um Iterable expondo o índice atual e o elemento.
   * Utiliza internamente um laço tradicional (while/Iterator) para maximizar
   * a previsibilidade de inlining do compilador JIT e zerar a alocação de objetos do tipo Stream.
   *
   * @param elements Iterable contendo os elementos (List, Set, etc).
   * @param consumer BiConsumer onde o primeiro argumento é o índice (0-based) e o segundo o elemento.
   * @param <T>      Tipo do elemento.
   */
  public static <T> void forEach(Iterable<T> elements, BiConsumer<Integer, ? super T> consumer) {
    Objects.requireNonNull(elements, "IndexedLoop: os elementos não podem ser nulos.");
    Objects.requireNonNull(consumer, "IndexedLoop: o consumer não pode ser nulo.");

    int index = 0;
    for (Iterator<T> iterator = elements.iterator(); iterator.hasNext(); ) {
      consumer.accept(index++, iterator.next());
    }
  }

  /**
   * Itera sobre um Array nativo expondo o índice.
   * Este é o caminho de ultra-baixa latência (Hot Path). Compila para um laço 'fori'
   * tradicional, permitindo que a JVM aplique Loop Unrolling e Bounds Check Elimination.
   *
   * @param elements Array de elementos.
   * @param consumer BiConsumer consumindo índice e elemento.
   */
  public static <T> void forEach(T[] elements, BiConsumer<Integer, ? super T> consumer) {
    Objects.requireNonNull(elements, "IndexedLoop: o array não pode ser nulo.");
    Objects.requireNonNull(consumer, "IndexedLoop: o consumer não pode ser nulo.");

    // O compilador C2 adora esse formato de laço. A performance é nativa.
    for (int i = 0; i < elements.length; i++) {
      consumer.accept(i, elements[i]);
    }
  }

  /**
   * Itera sobre uma List em ordem reversa expondo o índice original (0-based do começo).
   * Utiliza List.get(index) em ordem decrescente, evitando criação de Iterator ou cópia.
   *
   * <p>Ideal para listas com acesso O(1) (ArrayList, arrays). Para LinkedList, considere
   * converter para array primeiro, pois get(index) em LinkedList é O(n).</p>
   *
   * @param elements List contendo os elementos.
   * @param consumer BiConsumer onde o primeiro argumento é o índice original e o segundo o elemento.
   * @param <T>      Tipo do elemento.
   */
  public static <T> void forEachReverse(List<T> elements, BiConsumer<Integer, ? super T> consumer) {
    Objects.requireNonNull(elements, "IndexedLoop: a lista não pode ser nula.");
    Objects.requireNonNull(consumer, "IndexedLoop: o consumer não pode ser nulo.");

    for (int i = elements.size() - 1; i >= 0; i--) {
      consumer.accept(i, elements.get(i));
    }
  }

  /**
   * Itera sobre um Array em ordem reversa expondo o índice original (0-based do começo).
   * Este é o hot path para reverso: compila para laço decrescente puro.
   *
   * @param elements Array de elementos.
   * @param consumer BiConsumer consumindo índice e elemento.
   */
  public static <T> void forEachReverse(T[] elements, BiConsumer<Integer, ? super T> consumer) {
    Objects.requireNonNull(elements, "IndexedLoop: o array não pode ser nulo.");
    Objects.requireNonNull(consumer, "IndexedLoop: o consumer não pode ser nulo.");

    for (int i = elements.length - 1; i >= 0; i--) {
      consumer.accept(i, elements[i]);
    }
  }
}