package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utilitário stateless para execução de operações terminais seguras e otimizadas sobre coleções.
 * Foca em operações de short-circuiting e reduções estritas para o Domínio.
 */
public final class SearchUtils {

  private SearchUtils() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Operação terminal de short-circuiting. Verifica se algum elemento satisfaz a condição.
   * Para a iteração no primeiro 'true' encontrado.
   */
  public static <T> boolean containsAny(Collection<T> collection, Predicate<? super T> condition) {
    Objects.requireNonNull(collection, "SearchUtils: collection não pode ser nula.");
    Objects.requireNonNull(condition, "SearchUtils: condition não pode ser nula.");

    return collection.stream().anyMatch(condition);
  }

  /**
   * Operação terminal de redução estrita.
   * Busca um elemento na coleção que satisfaça a condição, mas garante que EXATAMENTE UM exista.
   * Se houver mais de um, lança exceção para proteger invariantes de negócio.
   */
  public static <T> Optional<T> findExactlyOne(Collection<T> collection, Predicate<? super T> condition) {
    Objects.requireNonNull(collection, "SearchUtils: collection não pode ser nula.");
    Objects.requireNonNull(condition, "SearchUtils: condition não pode ser nula.");

    return collection.stream()
      .filter(condition)
      .reduce((first, second) -> {
        throw new IllegalStateException("SearchUtils: Esperado exatamente 1 elemento, mas múltiplos foram encontrados.");
      });
  }

  public static <T> Optional<T> findLast(Collection<T> collection, Predicate<? super T> condition) {
    Objects.requireNonNull(collection, "SearchUtils: collection não pode ser nula.");
    Objects.requireNonNull(condition, "SearchUtils: condition não pode ser nula.");

    // Caminho otimizado para listas: percorre de trás para frente e faz short-circuit.
    if (collection instanceof List<?> rawList) {
      @SuppressWarnings("unchecked")
      List<T> list = (List<T>) rawList;
      for (int i = list.size() - 1; i >= 0; i--) {
        T element = list.get(i);
        if (condition.test(element)) {
          return Optional.of(element);
        }
      }
      return Optional.empty();
    }

    // Fallback para coleções gerais: varredura única mantendo o último match.
    T lastMatch = null;
    boolean found = false;
    for (T element : collection) {
      if (condition.test(element)) {
        lastMatch = element;
        found = true;
      }
    }

    return found ? Optional.of(lastMatch) : Optional.empty();
  }
}
