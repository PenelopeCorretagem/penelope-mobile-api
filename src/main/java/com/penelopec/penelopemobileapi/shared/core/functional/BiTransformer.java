package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.Objects;

/**
 * Interface funcional base para transformação semântica que exige duas entradas.
 *
 * @param <I1> Tipo do primeiro dado de entrada
 * @param <I2> Tipo do segundo dado de entrada
 * @param <O>  Tipo do dado de saída
 */
@FunctionalInterface
public interface BiTransformer<I1, I2, O> {

  /**
   * Aplica a transformação aos dados de entrada.
   *
   * @param input1 o primeiro dado
   * @param input2 o segundo dado
   * @return o resultado da transformação
   */
  O transform(I1 input1, I2 input2);

  /**
   * Retorna um BiTransformer composto que primeiro aplica esta transformação,
   * e então aplica o transformer 'after' ao resultado.
   *
   * @param <V> tipo de saída do transformer 'after'
   * @param after o transformer a ser aplicado após este
   * @return um BiTransformer composto
   */
  default <V> BiTransformer<I1, I2, V> andThen(Transformer<? super O, ? extends V> after) {
    Objects.requireNonNull(after, "BiTransformer: o próximo passo não pode ser nulo.");
    return (I1 input1, I2 input2) -> after.transform(this.transform(input1, input2));
  }
}