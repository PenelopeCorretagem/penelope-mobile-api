package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.Objects;

/**
 * Interface funcional base para transformação semântica de dados.
 * * @param <I> Tipo do dado de entrada
 * @param <O> Tipo do dado de saída
 */
@FunctionalInterface
public interface Transformer<I, O> {

  /**
   * Aplica a transformação ao dado de entrada.
   *
   * @param input o dado a ser transformado
   * @return o resultado da transformação
   */
  O transform(I input);

  /**
   * Retorna um transformer composto que primeiro aplica este transformer à sua entrada,
   * e então aplica o transformer 'after' ao resultado.
   *
   * @param <V> o tipo de saída do transformer 'after'
   * @param after o transformer a ser aplicado após este
   * @return um transformer composto
   * @throws NullPointerException se o transformer 'after' for nulo
   */
  default <V> Transformer<I, V> andThen(Transformer<? super O, ? extends V> after) {
    Objects.requireNonNull(after, "O próximo passo da transformação não pode ser nulo.");
    return (I input) -> after.transform(this.transform(input));
  }

  /**
   * Retorna um transformer composto que primeiro aplica o transformer 'before' à sua entrada,
   * e então aplica este transformer ao resultado.
   *
   * @param <V> o tipo de saída do transformer.
   * @param before o transformer a ser aplicado antes deste
   * @return um transformer composto
   * @throws NullPointerException se o transformer 'before' for nulo
   */
  default <V> Transformer<V, O> compose(Transformer<? super V, ? extends I> before) {
    Objects.requireNonNull(before, "O passo anterior da transformação não pode ser nulo.");
    return  (V input) -> this.transform(before.transform(input));
  }

  /**
   * Retorna um transformer que sempre retorna seu argumento de entrada (Identity).
   *
   * @param <T> o tipo de entrada e saída
   * @return um transformer de identidade
   */
  static <T> Transformer<T, T> identity() {
    return t -> t;
  }
}
