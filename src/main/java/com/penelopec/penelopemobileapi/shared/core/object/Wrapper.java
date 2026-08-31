package com.penelopec.penelopemobileapi.shared.core.object;

import java.util.*;

/**
 * Contrato padrão para objetos que favorecem Composição sobre Herança (Decorators/Proxies).
 * Fornece um mecanismo seguro e polimórfico para extrair a instância original
 * encapsulada, mesmo através de múltiplas camadas de composição.
 *
 * @param <T> o tipo base do objeto encapsulado.
 */
public interface Wrapper<T> {

  /**
   * Retorna a instância imediata que está sendo encapsulada (composta) por esta classe.
   *
   * @return A instância delegada diretamente conectada.
   */
  T unwrap();

  /**
   * Desempacota a cadeia de composição de forma recursiva (Deep Unwrap) até encontrar
   * a classe alvo desejada, suportando a extração segura de dependências subjacentes.
   * Blindado contra loops infinitos por referências circulares.
   *
   * @param targetType A classe alvo que se deseja encontrar na cadeia de composição.
   * @param <U> O tipo alvo inferido.
   * @return Um Optional contendo a instância encontrada, ou empty caso não exista.
   * @throws CyclicReferenceException Se um loop infinito for detectado.
   */
  default <U> Optional<U> unwrap(Class<U> targetType) {
    Objects.requireNonNull(targetType, "O tipo alvo não pode ser nulo.");

    Object current = this;
    // IdentityHashMap garante que a checagem de ciclo é baseada no ponteiro de memória (==),
    // e não no método equals(), evitando falsos positivos com proxies que repassam equals.
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    while (current != null) {
      // Verifica se o objeto atual já é o tipo buscado
      if (targetType.isInstance(current)) {
        return Optional.of(targetType.cast(current));
      }

      // Se não for um Wrapper, esgotamos a cadeia de composição e não achamos o tipo
      if (!(current instanceof Wrapper<?> wrapper)) {
        break;
      }

      // Previne loop infinito verificando se já passamos por esta instância exata
      if (!visited.add(current)) {
        throw new CyclicReferenceException("Ciclo de referência detectado na cadeia de composição do Wrapper.");
      }

      // Desce um nível na composição
      current = wrapper.unwrap();
    }

    return Optional.empty();
  }
}