package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilitário stateless para criação segura de Coleções Imutáveis (Java 9+).
 * * Previne falhas crônicas de NullPointerException ao converter coleções
 * que possam conter dados nulos indesejados, promovendo o padrão
 * de Defensive Copies sem fricção sintática.
 */
public final class ImmutableCollectionUtils {

  private ImmutableCollectionUtils() {
    throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada.");
  }

  /**
   * Retorna uma List estruturalmente imutável a partir da coleção original.
   * Ignora graciosamente coleções nulas e filtra elementos nulos internamente
   * para respeitar a restrição estrita das Immutable Collections do Java.
   *
   * @param source Coleção de origem (pode ser nula)
   * @param <T> Tipo do elemento
   * @return List verdadeiramente imutável e livre de nulos (nunca nula).
   */
  public static <T> List<T> safeImmutableList(Collection<T> source) {
    if (source == null || source.isEmpty()) {
      return List.of();
    }

    // toList() do Java 16+ já retorna lista imutável

    return source.stream()
      .filter(Objects::nonNull)
      .toList();
  }

  /**
   * Retorna um Set estruturalmente imutável a partir da coleção original.
   * Aplica proteção contra nulidade de coleções e elementos.
   *
   * @param source Coleção de origem (pode ser nula)
   * @param <T> Tipo do elemento
   * @return Set verdadeiramente imutável e livre de nulos (nunca nulo).
   */
  public static <T> Set<T> safeImmutableSet(Collection<T> source) {
    if (source == null || source.isEmpty()) {
      return Set.of();
    }
    return source.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toUnmodifiableSet()); // Garante Set.of() por baixo dos panos
  }

  public static <K, V> Map<K, V> safeImmutableMap(Map<K, V> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }

    Map<K, V> safeMap = MapUtils.newHashMapWithExpectedSize(source.size());
    source.forEach((k, v) -> {
      if (k != null && v != null) {
        safeMap.put(k, v);
      }
    });
    return Map.copyOf(safeMap);
  }
}
