package com.penelopec.penelopemobileapi.shared.core.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Either")
class EitherTest {

  @Nested
  @DisplayName("Quando operando com o lado Right (Sucesso)")
  class RightCases {

    @Test
    @DisplayName("Deve mapear o Right e reduzir via fold corretamente")
    void shouldMapRightAndFold() {
      Either<String, Integer> right = Either.right(100);

      String result = right
        .mapRight(val -> val * 2)
        .fold(
          leftErr -> "ERRO: " + leftErr,
          rightVal -> "SUCESSO: " + rightVal
        );

      assertThat(result).isEqualTo("SUCESSO: 200");
    }
  }

  @Nested
  @DisplayName("Quando operando com o lado Left (Erro)")
  class LeftCases {

    @Test
    @DisplayName("Deve ignorar o mapRight e reduzir para o left via fold")
    void shouldIgnoreMapRightAndFoldLeft() {
      Either<String, Integer> left = Either.left("Falha Critica");

      String result = left
        .mapRight(val -> val * 2) // Será ignorado
        .fold(
          leftErr -> "LIDADO: " + leftErr,
          rightVal -> "SUCESSO: " + rightVal
        );

      assertThat(result).isEqualTo("LIDADO: Falha Critica");
    }

    @Test
    @DisplayName("Deve aplicar fail-fast se inicializado com nulo")
    void shouldFailFastIfNull() {
      assertThatThrownBy(() -> Either.left(null))
        .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("Quando encadeando e executando efeitos colaterais (Result Parity)")
  class ResultParityCases {

    @Test
    @DisplayName("Deve executar onRight apenas quando for Right")
    void shouldExecuteOnRight() {
      AtomicBoolean rightCalled = new AtomicBoolean(false);
      AtomicBoolean leftCalled = new AtomicBoolean(false);

      Either.right("OK")
        .onRight(val -> rightCalled.set(true))
        .onLeft(err -> leftCalled.set(true));

      assertThat(rightCalled.get()).isTrue();
      assertThat(leftCalled.get()).isFalse();
    }

    @Test
    @DisplayName("Deve executar onLeft apenas quando for Left")
    void shouldExecuteOnLeft() {
      AtomicBoolean rightCalled = new AtomicBoolean(false);
      AtomicBoolean leftCalled = new AtomicBoolean(false);

      Either.left("ERR")
        .onRight(val -> rightCalled.set(true))
        .onLeft(err -> leftCalled.set(true));

      assertThat(rightCalled.get()).isFalse();
      assertThat(leftCalled.get()).isTrue();
    }

    @Test
    @DisplayName("Deve encadear monadicamente usando flatMapRight")
    void shouldChainWithFlatMapRight() {
      Either<String, Integer> right = Either.right(10);

      Either<String, String> flatMapped = right.flatMapRight(val -> {
        if (val > 5) {
          return Either.right("Maior que 5");
        }
        return Either.left("Menor ou igual a 5");
      });

      assertThat(flatMapped.isRight()).isTrue();
      flatMapped.onRight(val -> assertThat(val).isEqualTo("Maior que 5"));
    }
  }
}