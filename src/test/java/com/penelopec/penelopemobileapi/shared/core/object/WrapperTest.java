package com.penelopec.penelopemobileapi.shared.core.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Wrapper")
class WrapperTest {

  // ==========================================
  // Classes Mockadas para Cenários de Teste
  // ==========================================
  interface Task {}
  static class CoreTask implements Task {}

  // Aproveitando o novo AbstractWrapper para reduzir boilerplate no teste!
  static class AsyncWrapper extends AbstractWrapper<Task> implements Task {
    AsyncWrapper(Task delegate) { super(delegate); }
  }

  static class RetryWrapper extends AbstractWrapper<Task> implements Task {
    RetryWrapper(Task delegate) { super(delegate); }
  }

  // Proxy malicioso criado exclusivamente para simular ciclos infinitos
  static class CyclicWrapper implements Task, Wrapper<Task> {
    Task delegate;
    @Override public Task unwrap() { return delegate; }
  }

  // ==========================================
  // Testes
  // ==========================================

  @Nested
  @DisplayName("Extração Imediata (unwrap)")
  class ImmediateUnwrap {

    @Test
    @DisplayName("Deve retornar diretamente o delegado imediato")
    void shouldReturnImmediateDelegate() {
      Task core = new CoreTask();
      Wrapper<Task> asyncTask = new AsyncWrapper(core);

      assertThat(asyncTask.unwrap()).isSameAs(core);
    }
  }

  @Nested
  @DisplayName("Extração Profunda (unwrap com TargetType)")
  class DeepUnwrap {

    @Test
    @DisplayName("Deve atravessar a cadeia de composição para encontrar a classe alvo")
    void shouldTraverseCompositionChainToFindTargetClass() {
      Task core = new CoreTask();
      Task asyncTask = new AsyncWrapper(core);
      Wrapper<Task> fullyWrapped = new RetryWrapper(asyncTask);

      Optional<AsyncWrapper> foundAsync = fullyWrapped.unwrap(AsyncWrapper.class);
      Optional<CoreTask> foundCore = fullyWrapped.unwrap(CoreTask.class);

      assertThat(foundAsync).isPresent().containsSame((AsyncWrapper) asyncTask);
      assertThat(foundCore).isPresent().containsSame((CoreTask) core);
    }

    @Test
    @DisplayName("Deve retornar a si mesmo se o alvo for o próprio tipo do Wrapper")
    void shouldReturnSelfWhenTargetIsCurrentWrapper() {
      Task core = new CoreTask();
      Wrapper<Task> fullyWrapped = new RetryWrapper(core);

      Optional<RetryWrapper> foundRetry = fullyWrapped.unwrap(RetryWrapper.class);

      assertThat(foundRetry).isPresent().containsSame((RetryWrapper) fullyWrapped);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando a classe não existir na cadeia")
    void shouldReturnEmptyWhenTypeNotInChain() {
      Task core = new CoreTask();
      Wrapper<Task> retryOnly = new RetryWrapper(core);

      Optional<AsyncWrapper> notFound = retryOnly.unwrap(AsyncWrapper.class);

      assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Deve interromper a busca e lançar CyclicReferenceException em loops infinitos")
    void shouldThrowExceptionWhenCyclicReferenceDetected() {
      CyclicWrapper wrapperA = new CyclicWrapper();
      CyclicWrapper wrapperB = new CyclicWrapper();

      // Configurando o ciclo infinito intencional: A aponta para B, B aponta para A
      wrapperA.delegate = wrapperB;
      wrapperB.delegate = wrapperA;

      assertThatThrownBy(() -> wrapperA.unwrap(CoreTask.class))
        .isInstanceOf(CyclicReferenceException.class)
        .hasMessageContaining("Ciclo de referência detectado");
    }
  }
}