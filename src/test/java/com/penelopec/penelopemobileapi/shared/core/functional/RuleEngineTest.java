package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RuleEngine")
class RuleEngineTest {

  @Nested
  @DisplayName("Quando avaliar regras via Lambdas")
  class EvaluateRules {

    @Test
    @DisplayName("Deve retornar true se todas as regras passarem (Non-capturing lambdas)")
    void shouldReturnTrueWhenAllRulesPass() {
      RuleEngine<String> engine = RuleEngine.<String>create()
        .addRule(Objects::nonNull)
        .addRule(s -> !s.isEmpty())
        .addRule(s -> s.length() >= 4);

      assertThat(engine.evaluate("Java")).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false na primeira regra que falhar (Capturing lambda com variável efetivamente final)")
    void shouldReturnFalseWhenAnyRuleFailsWithClosure() {
      int minimumLength = 10; // Variável efetivamente final capturada na closure

      RuleEngine<String> engine = RuleEngine.<String>create()
        .addRule(s -> !s.isEmpty())
        .addRule(s -> s.length() >= minimumLength); // Closure

      assertThat(engine.evaluate("Java")).isFalse();
    }

    @Test
    @DisplayName("Deve falhar rápido se a regra ou o alvo forem nulos")
    void shouldThrowExceptionWhenInputsAreNull() {
      RuleEngine<String> engine = RuleEngine.create();

      assertThatThrownBy(() -> engine.addRule(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("RuleEngine: a regra não pode ser nula.");

      engine.addRule(s -> !s.isEmpty());

      assertThatThrownBy(() -> engine.evaluate(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("RuleEngine: o alvo da avaliação não pode ser nulo.");
    }
  }

  @Nested
  @DisplayName("Quando adicionar regras condicionalmente via addRuleIf")
  class AddRuleIf {

    @Test
    @DisplayName("Deve adicionar a regra quando a condição for true")
    void shouldAddRuleWhenConditionIsTrue() {
      RuleEngine<String> engine = RuleEngine.<String>create()
        .addRuleIf(true, s -> s.length() >= 4);

      assertThat(engine.evaluate("Java")).isTrue();
      assertThat(engine.evaluate("Go")).isFalse();
    }

    @Test
    @DisplayName("Não deve adicionar a regra quando a condição for false")
    void shouldNotAddRuleWhenConditionIsFalse() {
      RuleEngine<String> engine = RuleEngine.<String>create()
        .addRuleIf(false, s -> s.length() >= 100); // Regra jamais será avaliada

      assertThat(engine.evaluate("Go")).isTrue(); // Sem regras, sempre passa
    }

    @Test
    @DisplayName("Deve lançar exceção se a regra for nula e a condição for true")
    void shouldThrowWhenRuleIsNullAndConditionIsTrue() {
      RuleEngine<String> engine = RuleEngine.create();

      assertThatThrownBy(() -> engine.addRuleIf(true, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("RuleEngine: a regra não pode ser nula.");
    }

    @Test
    @DisplayName("Não deve lançar exceção se a regra for nula e a condição for false")
    void shouldNotThrowWhenRuleIsNullAndConditionIsFalse() {
      RuleEngine<String> engine = RuleEngine.create();

      // Condição false: a regra nula nunca é inspecionada
      engine.addRuleIf(false, null);

      assertThat(engine.evaluate("Java")).isTrue();
    }

    @Test
    @DisplayName("Deve permitir encadeamento fluente entre addRule e addRuleIf")
    void shouldSupportFluentChainingWithAddRule() {
      boolean applyLengthRule = true;

      RuleEngine<String> engine = RuleEngine.<String>create()
        .addRule(Objects::nonNull)
        .addRuleIf(applyLengthRule, s -> s.length() >= 4)
        .addRule(s -> s.startsWith("J"));

      assertThat(engine.evaluate("Java")).isTrue();
      assertThat(engine.evaluate("Go")).isFalse();
      assertThat(engine.evaluate("Python")).isFalse();
    }
  }
}