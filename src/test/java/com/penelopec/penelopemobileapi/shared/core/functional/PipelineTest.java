package com.penelopec.penelopemobileapi.shared.core.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pipeline")
class PipelineTest {

  @Nested
  @DisplayName("Quando processar um dado em fluxo")
  class ExecuteFlow {

    @Test
    @DisplayName("Deve executar todos os passos na ordem correta")
    void shouldExecuteAllStepsInOrder() {
      Pipeline<String> textProcessor = Pipeline.<String>create()
        .addStep(String::trim)
        .addStep(String::toLowerCase)
        .addStep(s -> s.replace('a', '@'));

      String result = textProcessor.execute("  JAVA Core  ");

      // A ordem importa: primeiro cortou espaços, depois minúscula, depois substituiu
      assertThat(result).isEqualTo("j@v@ core");
    }

    @Test
    @DisplayName("Deve ser imutável e seguro para derivação")
    void shouldBeImmutableAndSafeToDerive() {
      Pipeline<String> baseProcessor = Pipeline.<String>create()
        .addStep(String::trim);

      // Derivando dois fluxos distintos do mesmo base
      Pipeline<String> upperProcessor = baseProcessor.addStep(String::toUpperCase);
      Pipeline<String> lowerProcessor = baseProcessor.addStep(String::toLowerCase);

      assertThat(upperProcessor.execute(" Java ")).isEqualTo("JAVA");
      assertThat(lowerProcessor.execute(" Java ")).isEqualTo("java");
    }

    @Test
    @DisplayName("Deve retornar a entrada intacta caso não haja passos (Identity)")
    void shouldReturnSameInputWhenNoStepsAdded() {
      Pipeline<String> emptyPipeline = Pipeline.create();

      String result = emptyPipeline.execute("Intacto");

      assertThat(result).isEqualTo("Intacto");
    }

    @Test
    @DisplayName("Deve falhar rápido protegendo o motor contra nulidade")
    void shouldFailFastWhenInputOrStepIsNull() {
      Pipeline<String> pipeline = Pipeline.create();

      assertThatThrownBy(() -> pipeline.addStep(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Pipeline: o passo da transformação não pode ser nulo.");

      assertThatThrownBy(() -> pipeline.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Pipeline: a entrada para processamento não pode ser nula.");
    }
  }

  @Nested
  @DisplayName("Quando observar o fluxo via peek")
  class Peek {

    @Test
    @DisplayName("Deve executar a ação sem alterar o dado que flui pelo pipeline")
    void shouldExecuteActionWithoutAlteringData() {
      List<String> observed = new ArrayList<>();

      Pipeline<String> pipeline = Pipeline.<String>create()
        .addStep(String::trim)
        .peek(observed::add)           // captura o estado intermediário
        .addStep(String::toUpperCase);

      String result = pipeline.execute("  java  ");

      assertThat(result).isEqualTo("JAVA");
      assertThat(observed).containsExactly("java"); // trim aplicado, uppercase ainda não
    }

    @Test
    @DisplayName("Deve ser imutável: peek não afeta o pipeline base")
    void shouldBeImmutable() {
      List<String> log = new ArrayList<>();

      Pipeline<String> base = Pipeline.<String>create().addStep(String::trim);
      Pipeline<String> withPeek = base.peek(log::add);

      base.execute("  java  ");   // não aciona o peek
      withPeek.execute("  java  ");

      assertThat(log).hasSize(1).containsExactly("java");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a ação for nula")
    void shouldThrowWhenActionIsNull() {
      Pipeline<String> pipeline = Pipeline.create();

      assertThatThrownBy(() -> pipeline.peek(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Pipeline: a ação de observação não pode ser nula.");
    }
  }
}