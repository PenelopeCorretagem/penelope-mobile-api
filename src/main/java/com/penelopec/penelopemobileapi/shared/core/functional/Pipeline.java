package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Pipeline imutável para composição fluente de múltiplas transformações sobre um mesmo tipo de dado.
 * Segue o padrão arquitetural Pipes and Filters de forma funcional.
 *
 * @param <T> o tipo do dado que flui pelo pipeline
 */
public final class Pipeline<T> {

  private final Transformer<T, T> flow;

  private Pipeline(Transformer<T, T> flow) {
    this.flow = flow;
  }

  /**
   * Cria um Pipeline vazio.
   * Utiliza o padrão Identity (retorna a própria entrada) como base matemática.
   *
   * @param <T> o tipo do dado
   * @return um novo Pipeline
   */
  public static <T> Pipeline<T> create() {
    return new Pipeline<>(Transformer.identity());
  }

  /**
   * Adiciona um novo passo de transformação ao pipeline de forma imutável.
   *
   * @param step o transformador a ser aplicado
   * @return uma NOVA instância de Pipeline com o passo adicionado
   */
  public Pipeline<T> addStep(Transformer<T, T> step) {
    Objects.requireNonNull(step, "Pipeline: o passo da transformação não pode ser nulo.");
    return new Pipeline<>(this.flow.andThen(step));
  }

  /**
   * Executa o pipeline completo sobre a entrada fornecida.
   *
   * @param input o dado a ser processado
   * @return o resultado após todas as transformações
   */
  public T execute(T input) {
    Objects.requireNonNull(input, "Pipeline: a entrada para processamento não pode ser nula.");
    return flow.transform(input);
  }

  /**
   * Adiciona um passo de observação ao pipeline de forma imutável.
   * A ação é executada sobre o dado corrente sem alterá-lo, útil para logging e trace.
   *
   * @param action a ação a ser executada sobre o dado (sem modificá-lo)
   * @return uma NOVA instância de Pipeline com o passo de observação adicionado
   */
  public Pipeline<T> peek(Consumer<T> action) {
    Objects.requireNonNull(action, "Pipeline: a ação de observação não pode ser nula.");
    return new Pipeline<>(this.flow.andThen(input -> {
      action.accept(input);
      return input;
    }));
  }
}