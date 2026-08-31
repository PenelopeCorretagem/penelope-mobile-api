package com.penelopec.penelopemobileapi.shared.core.object;

/**
 * Esqueleto de implementação para {@link Equivalence} que padroniza
 * a checagem de referências de memória e o tratamento de nulos.
 * <p>
 * Favorece o padrão Template Method, delegando a regra de negócio central
 * para as subclasses concretas através dos métodos protected.
 */
public abstract class AbstractEquivalence<T> implements Equivalence<T> {

  @Override
  public final boolean equivalent(T a, T b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    return doEquivalent(a, b);
  }

  @Override
  public final int hash(T t) {
    if (t == null) {
      return 0;
    }
    return doHash(t);
  }

  /**
   * Implementado pelas subclasses para prover a equivalência real.
   * Garantia: Nenhum dos parâmetros será nulo, e eles não serão a mesma referência.
   */
  protected abstract boolean doEquivalent(T a, T b);

  /**
   * Implementado pelas subclasses para prover o cálculo do hash.
   * Garantia: O objeto nunca será nulo.
   */
  protected abstract int doHash(T t);
}
