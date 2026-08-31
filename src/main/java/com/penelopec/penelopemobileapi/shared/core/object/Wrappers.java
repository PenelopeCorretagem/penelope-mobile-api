package com.penelopec.penelopemobileapi.shared.core.object;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Utilitário para manipulação e criação de cadeias de composição (Wrappers).
 */
public final class Wrappers {

  private Wrappers() {
    throw new UnsupportedOperationException("Classe utilitária não instanciável.");
  }

  /**
   * Navega até o fundo da cadeia de composição para extrair a "raiz" (Root).
   * Retorna o primeiro objeto da cadeia que NÃO é um Wrapper.
   * Ideal para resgatar a entidade original de domínio por trás de dezenas de proxies.
   *
   * @param wrapper O wrapper inicial.
   * @param <T> O tipo esperado da raiz.
   * @return O objeto original no núcleo da composição.
   * @throws CyclicReferenceException se houver um loop infinito na cadeia.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getRoot(Wrapper<T> wrapper) {
    Objects.requireNonNull(wrapper, "O wrapper não pode ser nulo.");

    Object current = wrapper;
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    // Desce a árvore utilizando Pattern Matching (Java 16+)
    while (current instanceof Wrapper<?> w) {
      if (!visited.add(current)) {
        throw new CyclicReferenceException("Ciclo de referência detectado ao buscar a raiz do Wrapper.");
      }
      current = w.unwrap();
    }

    return (T) current;
  }

  /**
   * Verifica de forma segura se um objeto arbitrário possui composição.
   *
   * @param obj O objeto a ser avaliado.
   * @return true se o objeto implementar a interface Wrapper, false caso contrário.
   */
  public static boolean isWrapped(Object obj) {
    return obj instanceof Wrapper<?>;
  }

  /**
   * Cria um Wrapper "transparente" em tempo de execução.
   * Muito útil como um Adapter genérico quando uma API exige estritamente um
   * tipo Wrapper<T>, mas você só possui a instância original legada.
   *
   * @param delegate O objeto a ser envelopado.
   * @param <T> O tipo do objeto.
   * @return Uma instância simples de Wrapper contendo o objeto.
   */
  public static <T> Wrapper<T> of(T delegate) {
    return new SimpleWrapper<>(delegate);
  }

  // ==========================================
  // Implementações Internas
  // ==========================================

  /**
   * Implementação concreta e enxuta para adaptação em tempo de execução.
   */
  private static final class SimpleWrapper<T> extends AbstractWrapper<T> {
    SimpleWrapper(T delegate) {
      super(delegate);
    }
  }
}
