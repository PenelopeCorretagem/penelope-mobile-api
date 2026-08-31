package com.penelopec.penelopemobileapi.shared.core.object;

/**
 * Factory utilitária que provê instâncias comuns de {@link Equivalence}.
 */
public final class Equivalences {

  private Equivalences() {
    throw new UnsupportedOperationException("Classe utilitária não instanciável.");
  }

  /**
   * Equivalência que delega a comparação para o método nativo {@link Object#equals(Object)}.
   */
  @SuppressWarnings("unchecked")
  public static <T> Equivalence<T> nativeEquals() {
    return (Equivalence<T>) NativeEqualsEquivalence.INSTANCE;
  }

  /**
   * Equivalência estrita baseada na referência de memória (operador ==).
   */
  @SuppressWarnings("unchecked")
  public static <T> Equivalence<T> identity() {
    return (Equivalence<T>) IdentityEquivalence.INSTANCE;
  }

  /**
   * Equivalência exclusiva para Strings ignorando letras maiúsculas ou minúsculas.
   */
  public static Equivalence<String> ignoreCase() {
    return IgnoreCaseEquivalence.INSTANCE;
  }

  // ==========================================
  // Implementações Internas (Padrão Flyweight)
  // ==========================================

  private static final class NativeEqualsEquivalence extends AbstractEquivalence<Object> {
    static final NativeEqualsEquivalence INSTANCE = new NativeEqualsEquivalence();
    @Override protected boolean doEquivalent(Object a, Object b) { return a.equals(b); }
    @Override protected int doHash(Object t) { return t.hashCode(); }
  }

  private static final class IdentityEquivalence extends AbstractEquivalence<Object> {
    static final IdentityEquivalence INSTANCE = new IdentityEquivalence();
    // Se chegar aqui, o AbstractEquivalence já garantiu que (a != b), logo é falso.
    @Override protected boolean doEquivalent(Object a, Object b) { return false; }
    @Override protected int doHash(Object t) { return System.identityHashCode(t); }
  }

  private static final class IgnoreCaseEquivalence extends AbstractEquivalence<String> {
    static final IgnoreCaseEquivalence INSTANCE = new IgnoreCaseEquivalence();
    @Override protected boolean doEquivalent(String a, String b) { return a.equalsIgnoreCase(b); }
    @Override protected int doHash(String t) { return t.toLowerCase().hashCode(); }
  }
}
