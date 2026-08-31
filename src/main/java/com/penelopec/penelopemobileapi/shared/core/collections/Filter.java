package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.Objects;

/**
 * Contrato funcional para filtragem de elementos.
 * Criado para desacoplar lógicas de seleção das estruturas de dados,
 * permitindo composição declarativa de regras de negócio.
 *
 * @param <T> O tipo do objeto a ser avaliado.
 */
@FunctionalInterface
public interface Filter<T> {

  boolean matches(T item);

  /**
   * Compõe este filtro com outro através de um AND lógico.
   */
  default Filter<T> and(Filter<? super T> other) {
    Objects.requireNonNull(other, "O filtro secundário não pode ser nulo.");
    return item -> this.matches(item) && other.matches(item);
  }

  /**
   * Cria um filtro baseado em limite numérico/comparável.
   * Utiliza Bounded Types e Contravariância (? super T) para aceitar
   * classes filhas cujos pais definiram o contrato de comparação.
   */
  static <T extends Comparable<? super T>> Filter<T> greaterThan(T threshold) {
    Objects.requireNonNull(threshold, "O limite (threshold) não pode ser nulo.");
    return item -> {
      if (item == null) return false;
      return item.compareTo(threshold) > 0;
    };
  }

  /**
   * Cria um filtro complexo demonstrando MULTIPLE BOUNDS.
   * O tipo T é forçado a ser, SIMULTANEAMENTE, uma sequência de caracteres (String, etc)
   * E implementador de Comparable.
   */
  static <T extends CharSequence & Comparable<? super T>> Filter<T> textGreaterThan(int minLength, T threshold) {
    Objects.requireNonNull(threshold, "O limite de texto não pode ser nulo.");
    return item -> {
      if (item == null) return false;
      // Chama length() de CharSequence e compareTo() de Comparable com total type-safety
      return item.length() >= minLength && item.compareTo(threshold) > 0;
    };
  }

  /**
   * Filtra números pares utilizando o limite base Number.
   * Como Number não possui operadores matemáticos, utilizamos longValue()
   * para truncar decimais e extrair o valor bruto de forma segura.
   */
  static <T extends Number> Filter<T> isEven() {
    return item -> {
      if (item == null) return false;
      return item.longValue() % 2 == 0;
    };
  }
}