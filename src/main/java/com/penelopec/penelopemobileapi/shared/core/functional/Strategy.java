package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Roteador funcional baseado no padrão Strategy.
 * Avalia um dado de entrada contra uma série de condições e executa a
 * primeira transformação que corresponder à regra, retornando de forma segura (Optional).
 * <p>
 * Permite também o registro de ganchos de execução (pré-processadores) executados
 * sequencialmente antes da avaliação das estratégias.
 *
 * @param <I> o tipo do dado de entrada
 * @param <O> o tipo do dado de saída
 */
public final class Strategy<I, O> {

  private final List<RuleActionPair<I, O>> strategies = new ArrayList<>();
  private final List<Consumer<I>> preProcessors = new ArrayList<>();

  private Strategy() {}

  /**
   * Cria uma nova instância de roteamento Strategy.
   *
   * @param <I> tipo de entrada
   * @param <O> tipo de saída
   * @return nova instância
   */
  public static <I, O> Strategy<I, O> create() {
    return new Strategy<>();
  }

  /**
   * Registra um gancho de ciclo de vida (Side-Effect) que será executado para cada
   * entrada imediatamente antes de avaliar as estratégias lógicas.
   * <p>
   * O porquê deste método: Permite isolar lógicas transversais como logs de auditoria,
   * limpeza/sanitização prévia de dados ou tracking de telemetria sem poluir as regras core.
   * <p>
   * Retorna {@code this} para permitir encadeamento fluente junto com {@link #add}.
   *
   * @param preProcessor a ação funcional a ser executada (Consumer)
   * @return a própria instância para encadeamento fluente
   * @throws NullPointerException se o pré-processador for nulo
   */
  public Strategy<I, O> onBeforeEach(Consumer<I> preProcessor) {
    Objects.requireNonNull(preProcessor, "Strategy: o pré-processador interceptador não pode ser nulo.");
    this.preProcessors.add(preProcessor);
    return this;
  }

  /**
   * Registra uma nova estratégia condicional.
   * <p>
   * A ação é tipada como {@link Transformer}{@code <I, O>} (JDK padrão), eliminando a necessidade
   * de uma interface funcional customizada {@code Transformer} e reduzindo a superfície da API.
   *
   * @param condition a condição que aciona a estratégia (Predicate)
   * @param action    a ação a ser executada se a condição for verdadeira (Transformer)
   * @return a própria instância para encadeamento fluente
   * @throws NullPointerException se condition ou action forem nulos
   */
  public Strategy<I, O> add(Predicate<I> condition, Transformer<I, O> action) {
    Objects.requireNonNull(condition, "Strategy: a condição não pode ser nula.");
    Objects.requireNonNull(action, "Strategy: a ação não pode ser nula.");
    this.strategies.add(new RuleActionPair<>(condition, action));
    return this;
  }

  /**
   * Executa o roteamento. Primeiramente dispara todos os pré-processadores registrados
   * e, em seguida, avalia as condições na ordem de registro, aplicando a primeira transformação válida.
   *
   * @param input o dado de entrada a ser avaliado e transformado
   * @return Optional contendo o resultado da transformação, ou vazio se nenhuma condição bater
   * @throws NullPointerException se o input for nulo
   */
  public Optional<O> execute(I input) {
    Objects.requireNonNull(input, "Strategy: a entrada para execução não pode ser nula.");

    for (Consumer<I> preProcessor : preProcessors) {
      preProcessor.accept(input);
    }

    for (RuleActionPair<I, O> strategy : strategies) {
      if (strategy.condition().test(input)) {
        return Optional.ofNullable(strategy.action().transform(input));
      }
    }
    return Optional.empty();
  }

  /**
   * Executa o roteamento com um fallback garantido caso nenhuma condição bata.
   *
   * @param input         o dado de entrada
   * @param defaultAction ação padrão a ser executada se nenhuma outra corresponder
   * @return o resultado da transformação
   * @throws NullPointerException se defaultAction for nulo
   */
  public O executeOrElse(I input, Transformer<I, O> defaultAction) {
    Objects.requireNonNull(defaultAction, "Strategy: a ação de fallback não pode ser nula.");
    return execute(input).orElseGet(() -> defaultAction.transform(input));
  }

  /**
   * Executa o roteamento lançando uma exceção fornecida pelo chamador se nenhuma condição bater.
   * <p>
   * Use quando a ausência de match é um erro de programação — por exemplo, um tipo de evento
   * desconhecido que jamais deveria chegar ao roteador em produção. Para casos esperados de
   * "sem match", prefira {@link #execute} ou {@link #executeOrElse}.
   *
   * @param input             o dado de entrada
   * @param exceptionSupplier fábrica da exceção a ser lançada
   * @param <X>               tipo da exceção
   * @return o resultado da transformação
   * @throws X                    se nenhuma condição bater
   * @throws NullPointerException se exceptionSupplier for nulo
   */
  public <X extends RuntimeException> O executeOrThrow(I input, Supplier<X> exceptionSupplier) {
    Objects.requireNonNull(exceptionSupplier, "Strategy: o supplier de exceção não pode ser nulo.");
    return execute(input).orElseThrow(exceptionSupplier);
  }

  private record RuleActionPair<I, O>(Predicate<I> condition, Transformer<I, O> action) {}
}