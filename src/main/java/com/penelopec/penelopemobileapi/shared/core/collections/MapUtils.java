package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utilitário stateless para manipulação de Mapas com foco em eficiência
 * de alocação de memória e prevenção de Rehashing prematuro.
 */
public final class MapUtils {

  private static final float DEFAULT_LOAD_FACTOR = 0.75f;

  private MapUtils() {
    throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada.");
  }

  /**
   * Cria um HashMap devidamente pré-dimensionado para comportar a quantidade
   * de elementos esperada sem sofrer redimensionamento (Rehash).
   *
   * @param expectedSize Quantidade esperada de elementos.
   * @param <K> Tipo da chave
   * @param <V> Tipo do valor
   * @return Um HashMap vazio otimizado.
   * @throws IllegalArgumentException se expectedSize for negativo.
   */
  public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize) {
    return new HashMap<>(capacityForExpectedSize(expectedSize));
  }

  /**
   * Cria um novo LinkedHashMap (preserva a ordem de inserção) devidamente
   * pré-dimensionado.
   *
   * @param expectedSize Quantidade esperada de elementos.
   * @param <K> Tipo da chave
   * @param <V> Tipo do valor
   * @return Um LinkedHashMap vazio otimizado.
   */
  public static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
    return new LinkedHashMap<>(capacityForExpectedSize(expectedSize));
  }

  /**
   * Mescla de forma segura as chaves e valores do mapa de origem no mapa de destino.
   * Em caso de conflito, mantém o valor existente no mapa de destino.
   *
   * @param source Mapa de origem (pode ser nulo)
   * @param target Mapa de destino (não pode ser nulo)
   * @param <K> Tipo da chave
   * @param <V> Tipo do valor
   */
  public static <K, V> void mergeSafe(Map<? extends K, ? extends V> source, Map<K, V> target) {
    Objects.requireNonNull(target, "O mapa de destino não pode ser nulo.");
    if (source != null && !source.isEmpty()) {
      source.forEach(target::putIfAbsent);
    }
  }

  /**
   * Filtra um mapa com base num predicado aplicado às chaves.
   * Retorna um novo mapa contendo apenas as entradas cujas chaves forem aprovadas.
   *
   * <p>O mapa resultante é previamente dimensionado com base no tamanho do mapa de origem,
   * evitando rehash prematuro mesmo no pior caso em que todas as chaves sejam mantidas.</p>
   *
   * @param source mapa de origem
   * @param keyPredicate predicado aplicado às chaves
   * @param <K> tipo da chave
   * @param <V> tipo do valor
   * @return novo mapa contendo apenas as chaves aprovadas
   * @throws NullPointerException se source ou keyPredicate forem nulos
   */
  public static <K, V> Map<K, V> filter(Map<K, V> source, Predicate<? super K> keyPredicate) {
    Objects.requireNonNull(source, "MapUtils: o mapa de origem não pode ser nulo.");
    Objects.requireNonNull(keyPredicate, "MapUtils: o predicado da chave não pode ser nulo.");

    Map<K, V> filtered = newLinkedHashMapWithExpectedSize(source.size());
    source.forEach((key, value) -> {
      if (keyPredicate.test(key)) {
        filtered.put(key, value);
      }
    });

    return filtered;
  }

  /**
   * Calcula a capacidade inicial necessária para suportar o expectedSize
   * considerando o Load Factor padrão do Java (0.75).
   */
  private static int capacityForExpectedSize(int expectedSize) {
    if (expectedSize < 0) {
      throw new IllegalArgumentException("O tamanho esperado não pode ser negativo.");
    }
    if (expectedSize < 3) {
      return expectedSize + 1;
    }
    return (int) Math.ceil(expectedSize / DEFAULT_LOAD_FACTOR);
  }
}