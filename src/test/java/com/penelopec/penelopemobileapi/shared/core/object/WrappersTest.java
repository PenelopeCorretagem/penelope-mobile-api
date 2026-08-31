package com.penelopec.penelopemobileapi.shared.core.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Wrappers")
class WrappersTest {

  // Classes Mockadas
  interface Action {}
  static class CoreAction implements Action {}
  static class LogWrapper extends AbstractWrapper<Action> implements Action {
    LogWrapper(Action delegate) { super(delegate); }
  }
  static class TransactionWrapper extends AbstractWrapper<Action> implements Action {
    TransactionWrapper(Action delegate) { super(delegate); }
  }
  static class CyclicWrapper implements Action, Wrapper<Action> {
    Action delegate;
    @Override public Action unwrap() { return delegate; }
  }

  @Nested
  @DisplayName("Método getRoot")
  class GetRoot {

    @Test
    @DisplayName("Deve atravessar todas as camadas e retornar a entidade base")
    void shouldTraverseAllLayersAndReturnBaseEntity() {
      Action core = new CoreAction();
      Action logged = new LogWrapper(core);
      Wrapper<Action> fullyWrapped = new TransactionWrapper(logged);

      // independentemente de quantas camadas, queremos a Action original no fundo
      Action root = Wrappers.getRoot(fullyWrapped);

      assertThat(root).isSameAs(core);
    }

    @Test
    @DisplayName("Deve prevenir loops infinitos ao buscar a raiz")
    void shouldPreventInfiniteLoopsWhenFindingRoot() {
      CyclicWrapper w1 = new CyclicWrapper();
      CyclicWrapper w2 = new CyclicWrapper();
      w1.delegate = w2;
      w2.delegate = w1;

      assertThatThrownBy(() -> Wrappers.getRoot(w1))
        .isInstanceOf(CyclicReferenceException.class)
        .hasMessageContaining("Ciclo de referência detectado ao buscar a raiz");
    }
  }

  @Nested
  @DisplayName("Métodos isWrapped e of (Factory)")
  class IsWrappedAndOf {

    @Test
    @DisplayName("isWrapped deve identificar corretamente objetos compostos e originais")
    void isWrappedShouldCorrectlyIdentifyWrappers() {
      Action core = new CoreAction();
      Wrapper<Action> wrapper = new LogWrapper(core);

      assertThat(Wrappers.isWrapped(wrapper)).isTrue();
      assertThat(Wrappers.isWrapped(core)).isFalse();
      assertThat(Wrappers.isWrapped(null)).isFalse();
    }

    @Test
    @DisplayName("of deve envelopar transparentemente um objeto sem poluir a herança")
    void ofShouldTransparentlyWrapObject() {
      Action core = new CoreAction();
      Wrapper<Action> dummyWrapper = Wrappers.of(core);

      assertThat(dummyWrapper.unwrap()).isSameAs(core);
      // Comprova que podemos puxar via getRoot normalmente
      assertThat(Wrappers.getRoot(dummyWrapper)).isSameAs(core);
    }
  }
}