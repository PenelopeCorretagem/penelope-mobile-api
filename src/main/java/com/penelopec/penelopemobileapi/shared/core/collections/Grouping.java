package com.penelopec.penelopemobileapi.shared.core.collections;


import com.penelopec.penelopemobileapi.shared.core.functional.Transformer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Encapsula o agrupamento de coleções, garantindo imutabilidade profunda e
 * proteção segura contra chaves de agrupamento nulas.
 *
 * @param <K> Tipo da chave de agrupamento
 * @param <T> Tipo do elemento agrupado
 */
public final class Grouping<K, T> {

  private final Map<K, List<T>> groups;

  private Grouping(Map<K, List<T>> groups) {
    this.groups = groups;
  }

  /**
   * Agrupa uma coleção por uma função extratora de chaves.
   * Elementos cuja chave avaliada for nula serão ignorados preventivamente.
   */
  public static <K, T> Grouping<K, T> of(Collection<T> items, Function<? super T, ? extends K> keyExtractor) {
    Objects.requireNonNull(items, "Grouping: a coleção de itens não pode ser nula");
    Objects.requireNonNull(keyExtractor, "Grouping: a função extratora não pode ser nula");

    Map<K, List<T>> collected = items.stream()
      .filter(item -> item != null && keyExtractor.apply(item) != null)
      .collect(Collectors.collectingAndThen(
        Collectors.groupingBy(keyExtractor),
        map -> map.entrySet().stream()
          .collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            e -> List.copyOf(e.getValue()) // Deep immutability dos valores
          ))
      ));

    return new Grouping<>(collected);
  }

  /**
   * Retorna a lista de itens de um grupo específico.
   * Retorna lista vazia caso a chave não exista, evitando NullPointerExceptions no cliente.
   */
  public List<T> get(K key) {
    return this.groups.getOrDefault(key, Collections.emptyList());
  }

  /**
   * Retorna todas as chaves de agrupamento encontradas.
   */
  public Set<K> keys() {
    return this.groups.keySet();
  }

  /**
   * Expõe o mapa agrupado imutável nativo, caso seja necessário integração com APIs externas.
   */
  public Map<K, List<T>> asMap() {
    return this.groups;
  }

  /**
   * Transforma cada grupo (lista de valores por chave) numa nova lista de outro tipo,
   * gerando um novo Grouping imutável.
   *
   * <p>O mapper recebe a lista imutável atual associada a cada chave e deve retornar
   * uma nova lista para aquela chave. O resultado aplica cópia defensiva em cada lista
   * e no mapa final, preservando imutabilidade profunda.</p>
   *
   * @param groupMapper função que transforma a lista de cada grupo
   * @param <R> tipo dos elementos nas listas transformadas
   * @return novo Grouping com as mesmas chaves e listas transformadas
   * @throws NullPointerException se groupMapper for nulo
   * @throws NullPointerException se groupMapper retornar lista nula para alguma chave
   */
  public <R> Grouping<K, R> mapGroups(Transformer<? super List<T>, ? extends List<R>> groupMapper) {
    Objects.requireNonNull(groupMapper, "Grouping: o mapeador de grupos não pode ser nulo");

    Map<K, List<R>> mapped = this.groups.entrySet().stream()
      .collect(Collectors.toUnmodifiableMap(
        Map.Entry::getKey,
        entry -> {
          List<R> transformed = Objects.requireNonNull(
            groupMapper.transform(entry.getValue()),
            "Grouping: o mapeador de grupos não pode retornar lista nula"
          );
          return List.copyOf(transformed);
        }
      ));

    return new Grouping<>(mapped);
  }
}