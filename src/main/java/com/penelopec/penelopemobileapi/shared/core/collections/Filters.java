package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.Objects;

/**
 * Catálogo central de filtros utilitários de domínio.
 * <p>Implementa o padrão Utility Class para fornecer regras de filtragem
 * comuns, seguras contra nulos (null-safe) e reutilizáveis.</p>
 */
public final class Filters {

  private Filters() {
    throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada.");
  }

  /**
   * Filtra elementos garantindo que não sejam nulos.
   */
  public static <T> Filter<T> notNull() {
    return Objects::nonNull;
  }

  /**
   * Filtra números pares.
   * Utiliza longValue() para ignorar casas decimais. É null-safe.
   */
  public static <T extends Number> Filter<T> isEven() {
    return item -> item != null && item.longValue() % 2 == 0;
  }

  /**
   * Filtra textos que sejam nulos, vazios ou contenham apenas espaços em branco.
   * É null-safe.
   */
  public static <T extends CharSequence> Filter<T> isBlank() {
    return item -> item == null || item.toString().trim().isEmpty();
  }
}