package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Strategy")
class StrategyTest {

  // ---------------------------------------------------------------------------
  // Domínio compartilhado entre os testes
  // ---------------------------------------------------------------------------

  record Customer(String type, double balance) {
    boolean isVip()     { return "VIP".equals(type); }
    boolean isRegular() { return "REGULAR".equals(type); }
  }

  static class DiscountService {
    static double applyVipDiscount(Customer c)     { return c.balance() * 0.8;  }
    static double applyRegularDiscount(Customer c) { return c.balance() * 0.95; }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Quando executar estratégias registradas")
  class ExecuteStrategy {

    @Test
    @DisplayName("Deve acionar a estratégia correta via Method References")
    void shouldExecuteCorrectStrategyViaMethodReferences() {
      Strategy<Customer, Double> discountStrategy = Strategy.<Customer, Double>create()
        .add(Customer::isVip,     DiscountService::applyVipDiscount)
        .add(Customer::isRegular, DiscountService::applyRegularDiscount);

      assertThat(discountStrategy.execute(new Customer("VIP",     100.0))).contains(80.0);
      assertThat(discountStrategy.execute(new Customer("REGULAR", 100.0))).contains(95.0);
    }

    @Test
    @DisplayName("Deve retornar vazio se nenhuma estratégia der match")
    void shouldReturnEmptyWhenNoStrategyMatches() {
      Strategy<Customer, Double> discountStrategy = Strategy.<Customer, Double>create()
        .add(Customer::isVip, DiscountService::applyVipDiscount);

      assertThat(discountStrategy.execute(new Customer("NEW", 100.0))).isEmpty();
    }

    @Test
    @DisplayName("Deve executar o fallback com executeOrElse quando nenhuma estratégia combinar")
    void shouldExecuteFallbackWhenNoStrategyMatches() {
      Strategy<Customer, Double> discountStrategy = Strategy.<Customer, Double>create()
        .add(Customer::isVip, DiscountService::applyVipDiscount);

      Double finalValue = discountStrategy.executeOrElse(
        new Customer("GUEST", 100.0),
        Customer::balance
      );

      assertThat(finalValue).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Deve falhar rápido se proteger contra entradas nulas")
    void shouldFailFastOnNullInputs() {
      Strategy<String, String> strategy = Strategy.create();

      assertThatThrownBy(() -> strategy.add(null, String::toUpperCase))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Strategy: a condição não pode ser nula.");

      assertThatThrownBy(() -> strategy.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Strategy: a entrada para execução não pode ser nula.");
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("onBeforeEach — pré-processadores")
  class OnBeforeEach {

    @Test
    @DisplayName("Deve executar o pré-processador antes de avaliar as estratégias")
    void shouldRunPreProcessorBeforeStrategies() {
      List<String> audit = new ArrayList<>();

      Strategy.<Customer, Double>create()
        .onBeforeEach(c -> audit.add("processing: " + c.type()))
        .add(Customer::isVip, DiscountService::applyVipDiscount)
        .execute(new Customer("VIP", 100.0));

      assertThat(audit).containsExactly("processing: VIP");
    }

    @Test
    @DisplayName("Deve executar múltiplos pré-processadores na ordem de registro")
    void shouldRunMultiplePreProcessorsInOrder() {
      List<String> log = new ArrayList<>();

      Strategy.<Customer, Double>create()
        .onBeforeEach(c -> log.add("first"))
        .onBeforeEach(c -> log.add("second"))
        .add(Customer::isVip, DiscountService::applyVipDiscount)
        .execute(new Customer("VIP", 100.0));

      assertThat(log).containsExactly("first", "second");
    }

    @Test
    @DisplayName("Deve executar pré-processador mesmo quando nenhuma estratégia der match")
    void shouldRunPreProcessorEvenWithNoMatch() {
      List<String> audit = new ArrayList<>();

      Strategy.<Customer, Double>create()
        .onBeforeEach(c -> audit.add("seen: " + c.type()))
        .add(Customer::isVip, DiscountService::applyVipDiscount)
        .execute(new Customer("GUEST", 100.0));

      // o gancho disparou, mesmo que Optional.empty() tenha sido retornado
      assertThat(audit).containsExactly("seen: GUEST");
    }

    @Test
    @DisplayName("Deve retornar this para permitir encadeamento fluente com add")
    void shouldReturnThisForFluentChaining() {
      // Se onBeforeEach retornasse void, este encadeamento não compilaria.
      // O fato de compilar já valida o contrato de fluência.
      Strategy<Customer, Double> strategy = Strategy.<Customer, Double>create()
        .onBeforeEach(c -> {})
        .add(Customer::isVip, DiscountService::applyVipDiscount)
        .onBeforeEach(c -> {})     // intercalado deliberadamente
        .add(Customer::isRegular, DiscountService::applyRegularDiscount);

      assertThat(strategy.execute(new Customer("VIP", 200.0))).contains(160.0);
    }

    @Test
    @DisplayName("Deve lançar NullPointerException ao registrar pré-processador nulo")
    void shouldThrowOnNullPreProcessor() {
      assertThatThrownBy(() -> Strategy.create().onBeforeEach(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Strategy: o pré-processador interceptador não pode ser nulo.");
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("executeOrThrow — falha explícita sem match")
  class ExecuteOrThrow {

    static class UnknownCustomerTypeException extends RuntimeException {
      UnknownCustomerTypeException(String type) {
        super("Tipo de cliente desconhecido: " + type);
      }
    }

    @Test
    @DisplayName("Deve retornar o resultado quando uma estratégia der match")
    void shouldReturnResultWhenStrategyMatches() {
      Strategy<Customer, Double> strategy = Strategy.<Customer, Double>create()
        .add(Customer::isVip, DiscountService::applyVipDiscount);

      Double result = strategy.executeOrThrow(
        new Customer("VIP", 100.0),
        () -> new UnknownCustomerTypeException("VIP")
      );

      assertThat(result).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Deve lançar a exceção fornecida quando nenhuma estratégia der match")
    void shouldThrowProvidedExceptionWhenNoMatch() {
      Strategy<Customer, Double> strategy = Strategy.<Customer, Double>create()
        .add(Customer::isVip, DiscountService::applyVipDiscount);

      assertThatThrownBy(() ->
        strategy.executeOrThrow(
          new Customer("UNKNOWN", 100.0),
          () -> new UnknownCustomerTypeException("UNKNOWN")
        )
      )
        .isInstanceOf(UnknownCustomerTypeException.class)
        .hasMessage("Tipo de cliente desconhecido: UNKNOWN");
    }

    @Test
    @DisplayName("Deve lançar NullPointerException se o supplier de exceção for nulo")
    void shouldThrowOnNullExceptionSupplier() {
      Strategy<Customer, Double> strategy = Strategy.<Customer, Double>create()
        .add(Customer::isVip, DiscountService::applyVipDiscount);

      assertThatThrownBy(() ->
        strategy.executeOrThrow(new Customer("VIP", 100.0), null)
      )
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Strategy: o supplier de exceção não pode ser nulo.");
    }

    @Test
    @DisplayName("Deve disparar pré-processadores antes de lançar a exceção")
    void shouldRunPreProcessorsBeforeThrowing() {
      List<String> audit = new ArrayList<>();

      Strategy<Customer, Double> strategy = Strategy.<Customer, Double>create()
        .onBeforeEach(c -> audit.add("audited: " + c.type()))
        .add(Customer::isVip, DiscountService::applyVipDiscount);

      assertThatThrownBy(() ->
        strategy.executeOrThrow(
          new Customer("UNKNOWN", 50.0),
          () -> new UnknownCustomerTypeException("UNKNOWN")
        )
      ).isInstanceOf(UnknownCustomerTypeException.class);

      // O pré-processador rodou mesmo que a execução tenha terminado em exceção
      assertThat(audit).containsExactly("audited: UNKNOWN");
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Transformer<I,O> — compatibilidade com JDK")
  class TransformerCompatibility {

    @Test
    @DisplayName("Deve aceitar Transformer diretamente como ação")
    void shouldAcceptJdkTransformerAsAction() {
      Transformer<String, Integer> lengthFn = String::length;

      Strategy<String, Integer> strategy = Strategy.<String, Integer>create()
        .add(s -> !s.isBlank(), lengthFn);

      assertThat(strategy.execute("hello")).contains(5);
    }

    @Test
    @DisplayName("Deve compor Transformer com andThen antes de registrar")
    void shouldSupportTransformerCompositionBeforeRegistering() {
      Transformer<String, String> upper = String::toUpperCase;
      Transformer<String, String> trim  = String::strip;

      Strategy<String, String> strategy = Strategy.<String, String>create()
        .add(s -> !s.isBlank(), trim.andThen(upper));

      assertThat(strategy.execute("  hello  ")).contains("HELLO");
    }
  }
}