package com.penelopec.penelopemobileapi.shared.core.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Motor de regras funcional para avaliação fluente de múltiplos predicados (Lambdas).
 * Funciona sob a premissa de Fail-Fast: a avaliação é interrompida na primeira falha.
 *
 * @param <T> o tipo do objeto alvo da avaliação
 */
public final class RuleEngine<T> {

  private final List<Predicate<T>> rules;

  private RuleEngine() {
    this.rules = new ArrayList<>();
  }

  /**
   * Instancia um novo motor de regras vazio.
   * @param <T> o tipo do alvo
   * @return uma nova instância de RuleEngine
   */
  public static <T> RuleEngine<T> create() {
    return new RuleEngine<>();
  }

  /**
   * Adiciona uma regra (lambda) ao motor de avaliação.
   *
   * @param rule a regra funcional a ser adicionada
   * @return a própria instância para method chaining (fluência)
   */
  public RuleEngine<T> addRule(Predicate<T> rule) {
    Objects.requireNonNull(rule, "RuleEngine: a regra não pode ser nula.");
    this.rules.add(rule);
    return this;
  }

  /**
   * Adiciona uma regra ao motor somente se a condição fornecida for verdadeira.
   * Útil para lógicas de validação condicionais sem quebrar o encadeamento fluente.
   *
   * @param condition a condição que determina se a regra deve ser adicionada
   * @param rule      a regra funcional a ser adicionada condicionalmente
   * @return a própria instância para method chaining (fluência)
   */
  public RuleEngine<T> addRuleIf(boolean condition, Predicate<T> rule) {
    if (condition) {
      Objects.requireNonNull(rule, "RuleEngine: a regra não pode ser nula.");
      this.rules.add(rule);
    }
    return this;
  }

  /**
   * Avalia o objeto alvo contra todas as regras registradas.
   * Utiliza a API de Stream com semântica de curto-circuito (fail-fast):
   * a avaliação é interrompida na primeira regra que falhar.
   *
   * @param target o objeto a ser avaliado
   * @return true se todas as regras passarem, false na primeira que falhar
   */
  public boolean evaluate(T target) {
    Objects.requireNonNull(target, "RuleEngine: o alvo da avaliação não pode ser nulo.");
    return rules.stream().allMatch(rule -> rule.test(target));
  }
}