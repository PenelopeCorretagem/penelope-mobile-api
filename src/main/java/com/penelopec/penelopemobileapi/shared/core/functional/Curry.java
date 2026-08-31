package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.Objects;

/**
 * Utilitário para operações de alta ordem: Currying e Aplicação Parcial.
 * Totalmente integrado ao ecossistema de Transformers da commons-core.
 */
public final class Curry {

  private Curry() {
    throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
  }

  /**
   * Aplica Currying a um BiTransformer.
   * Transforma (I1, I2) -> O em I1 -> (I2 -> O).
   *
   * @param biTransformer a transformação original
   * @return um Transformer que gera outro Transformer
   */
  public static <I1, I2, O> Transformer<I1, Transformer<I2, O>> curry(BiTransformer<I1, I2, O> biTransformer) {
    Objects.requireNonNull(biTransformer, "Curry: o transformer base não pode ser nulo.");
    return input1 -> input2 -> biTransformer.transform(input1, input2);
  }

  /**
   * Realiza a Aplicação Parcial do primeiro argumento em um BiTransformer.
   * Reduz a aridade da transformação de 2 para 1.
   *
   * @param biTransformer a transformação original
   * @param fixedValue o valor a ser fixado como primeiro argumento
   * @return um novo Transformer que exige apenas o segundo argumento
   */
  public static <I1, I2, O> Transformer<I2, O> partial(BiTransformer<I1, I2, O> biTransformer, I1 fixedValue) {
    Objects.requireNonNull(biTransformer, "Curry: o transformer base não pode ser nulo.");
    // O input1 fica preso na closure criada por esta expressão lambda
    return input2 -> biTransformer.transform(fixedValue, input2);
  }
}
