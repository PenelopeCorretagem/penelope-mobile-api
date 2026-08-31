package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Curry")
class CurryTest {

  @Nested
  @DisplayName("Quando gerenciar o ciclo de vida da classe utilitária")
  class Instantiation {

    @Test
    @DisplayName("Deve lançar exceção ao tentar instanciar via reflexão")
    void shouldThrowExceptionWhenInstantiating() {
      assertThatThrownBy(() -> {
        Constructor<Curry> constructor = Curry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
          constructor.newInstance();
        } catch (InvocationTargetException e) {
          throw e.getCause(); // Desempacota a exceção real lançada no construtor
        }
      }).isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Classe utilitária não deve ser instanciada.");
    }
  }

  @Nested
  @DisplayName("Quando aplicar Currying")
  class CurryingTests {

    @Test
    @DisplayName("Deve converter BiTransformer em dois Transformers encadeados")
    void shouldConvertBiTransformerToCurriedTransformers() {
      BiTransformer<Double, Double, Double> calcTax = (rate, amount) -> amount + (amount * rate);

      Transformer<Double, Transformer<Double, Double>> curriedTax = Curry.curry(calcTax);

      // Passo 1: Recebe a taxa e retorna uma nova função
      Transformer<Double, Double> applyTenPercent = curriedTax.transform(0.10);

      // Passo 2: Recebe o valor e executa o cálculo final
      Double result = applyTenPercent.transform(100.0);

      assertThat(result).isEqualTo(110.0);
    }

    @Test
    @DisplayName("Deve falhar rápido se o BiTransformer base for nulo")
    void shouldThrowExceptionWhenBaseIsNull() {
      assertThatThrownBy(() -> Curry.curry(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Curry: o transformer base não pode ser nulo.");
    }
  }

  @Nested
  @DisplayName("Quando realizar Aplicação Parcial")
  class PartialApplicationTests {

    @Test
    @DisplayName("Deve fixar o primeiro argumento e retornar Transformer aplicável para o segundo")
    void shouldFixFirstArgumentAndReturnApplicableTransformer() {
      BiTransformer<String, String, String> formatLog = (level, msg) -> String.format("[%s] %s", level, msg);

      // Cria funções derivadas congelando o primeiro parâmetro (level)
      Transformer<String, String> warnLogger = Curry.partial(formatLog, "WARN");
      Transformer<String, String> errorLogger = Curry.partial(formatLog, "ERROR");

      assertThat(warnLogger.transform("Memória alta")).isEqualTo("[WARN] Memória alta");
      assertThat(errorLogger.transform("Falha no banco")).isEqualTo("[ERROR] Falha no banco");
    }

    @Test
    @DisplayName("Deve falhar rápido se o BiTransformer base for nulo na aplicação parcial")
    void shouldThrowExceptionWhenBaseIsNullForPartial() {
      assertThatThrownBy(() -> Curry.partial(null, "Valor Fixo"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Curry: o transformer base não pode ser nulo.");
    }
  }
}