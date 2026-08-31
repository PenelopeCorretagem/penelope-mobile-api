package com.penelopec.penelopemobileapi.shared.core.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Nested
@DisplayName("Testes do DynamicProxy")
class DynamicProxyTest {

  interface GreetingService {
    String sayHello(String name);
  }

  @Test
  @DisplayName("Deve interceptar e modificar o retorno de um método da interface")
  void shouldInterceptMethodCallWhenProxyIsCreated() {
    GreetingService proxy = DynamicProxy.create(GreetingService.class, (proxyObj, method, args) -> {
      if (method.getName().equals("sayHello")) {
        return "Intercepted: " + args[0];
      }
      return null;
    });

    String result = proxy.sayHello("Alice");
    assertThat(result).isEqualTo("Intercepted: Alice");
  }

  @Test
  @DisplayName("Deve falhar rápido se o alvo for uma classe concreta (não suportado na JDK)")
  void shouldFailWhenTargetIsConcreteClass() {
    assertThatThrownBy(() -> DynamicProxy.create(String.class, (p, m, a) -> null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("apenas suporta interfaces");
  }

  @Nested
  @DisplayName("Quando cria lista não modificável via proxy")
  class UnmodifiableList {

    @Test
    @DisplayName("Deve permitir leitura sem alterar o conteúdo")
    void shouldAllowReadOperations() {
      List<String> source = new ArrayList<>(List.of("A", "B", "C"));

      List<String> unmodifiable = DynamicProxy.unmodifiableList(source);

      assertThat(unmodifiable.size()).isEqualTo(3);
      assertThat(unmodifiable.get(1)).isEqualTo("B");
      assertThat(unmodifiable).containsExactly("A", "B", "C");
    }

    @Test
    @DisplayName("Deve bloquear mutações diretas da lista")
    void shouldBlockDirectMutations() {
      List<String> source = new ArrayList<>(List.of("A", "B"));
      List<String> unmodifiable = DynamicProxy.unmodifiableList(source);

      assertThatThrownBy(() -> unmodifiable.add("C"))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("somente leitura");

      assertThatThrownBy(() -> unmodifiable.remove("A"))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("somente leitura");
    }

    @Test
    @DisplayName("Deve bloquear mutações por iterator e subList")
    void shouldBlockMutationsViaIteratorAndSubList() {
      List<String> source = new ArrayList<>(List.of("A", "B", "C"));
      List<String> unmodifiable = DynamicProxy.unmodifiableList(source);

      var iterator = unmodifiable.iterator();
      iterator.next();
      assertThatThrownBy(iterator::remove)
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("somente leitura");

      List<String> subList = unmodifiable.subList(0, 2);
      assertThatThrownBy(() -> subList.add("X"))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("somente leitura");
    }
  }
}