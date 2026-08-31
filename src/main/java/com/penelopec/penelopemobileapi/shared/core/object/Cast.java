package com.penelopec.penelopemobileapi.shared.core.object;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utilitário para conversões de tipo (casting) seguras e polimórficas.
 */
public final class Cast {

  private Cast() {
    throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
  }

  /**
   * Tenta realizar o cast seguro de um objeto para o tipo especificado.
   *
   * @param instance A instância a ser avaliada.
   * @param type A classe do tipo alvo.
   * @return Um Optional contendo o objeto tipado, ou empty se o cast for inválido.
   */
  public static <T> Optional<T> as(Object instance, Class<T> type) {
    if (instance == null || type == null) {
      return Optional.empty();
    }

    if (type.isInstance(instance)) {
      return Optional.of(type.cast(instance));
    }

    return Optional.empty();
  }

  /**
   * Filtra uma coleção genérica, retornando um Stream apenas com os elementos
   * que correspondem ao tipo polimórfico especificado, já devidamente convertidos.
   *
   * @param collection A coleção original contendo elementos variados.
   * @param type A classe do tipo alvo para o filtro e cast.
   * @return Um Stream contendo apenas os elementos do tipo solicitado.
   */
  public static <T> Stream<T> filter(Collection<?> collection, Class<T> type) {
    if (collection == null || type == null) {
      return Stream.empty();
    }

    return collection.stream()
      .filter(type::isInstance)
      .map(type::cast); // Transforma Stream<Object> em Stream<T>
  }
}