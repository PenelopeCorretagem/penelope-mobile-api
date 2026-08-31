package com.penelopec.penelopemobileapi.shared.core.object;

import java.util.Iterator;

/**
 * Define uma estratégia para determinar a equivalência entre instâncias do tipo {@code T}.
 * Ao contrário do {@link Object#equals(Object)}, permite que regras de igualdade
 * sejam desacopladas da própria classe, suportando múltiplas visões de igualdade.
 *
 * @param <T> o tipo dos objetos a serem comparados.
 */
public interface Equivalence<T> {

  /**
   * Retorna true se os objetos são considerados equivalentes.
   */
  boolean equivalent(T a, T b);

  /**
   * Retorna um código de hash consistente com esta equivalência.
   */
  int hash(T t);

  /**
   * Retorna uma equivalência capaz de comparar dois iteráveis elemento a elemento,
   * garantindo a ordem e utilizando a regra de equivalência atual desta instância.
   */
  default Equivalence<Iterable<T>> pairwise() {
    // Captura a instância atual de Equivalence para uso dentro da classe anônima
    Equivalence<T> elementEquivalence = this;

    return new AbstractEquivalence<Iterable<T>>() {
      @Override
      protected boolean doEquivalent(Iterable<T> iterableA, Iterable<T> iterableB) {
        Iterator<T> iteratorA = iterableA.iterator();
        Iterator<T> iteratorB = iterableB.iterator();

        while (iteratorA.hasNext() && iteratorB.hasNext()) {
          if (!elementEquivalence.equivalent(iteratorA.next(), iteratorB.next())) {
            return false;
          }
        }
        // Se um iterável for maior que o outro, não são equivalentes
        return !iteratorA.hasNext() && !iteratorB.hasNext();
      }

      @Override
      protected int doHash(Iterable<T> iterable) {
        int hash = 7836; // Semente arbitrária para evitar colisões
        for (T element : iterable) {
          hash = hash * 31 + elementEquivalence.hash(element);
        }
        return hash;
      }
    };
  }
}
