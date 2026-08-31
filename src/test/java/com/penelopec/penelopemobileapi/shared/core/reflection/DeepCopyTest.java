package com.penelopec.penelopemobileapi.shared.core.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
@DisplayName("DeepCopy")

class DeepCopyTest {
  static class Node {
    String name;
    Node next;

    Node() {} // Exigido pelo DeepCopy
    Node(String name) { this.name = name; }
  }

  static class Dto {
    String title;
    Integer age;
  }

  static class NoDefaultCtorNode {
    String name;
    NoDefaultCtorNode next;

    NoDefaultCtorNode(String name) {
      this.name = name;
    }
  }

  @Test
  @DisplayName("Deve clonar objetos aninhados com referências distintas")
  void shouldDeepCloneNestedObjects() {
    Node original = new Node("A");
    original.next = new Node("B");

    Node clone = DeepCopy.of(original);

    assertThat(clone).isNotSameAs(original);
    assertThat(clone.name).isEqualTo("A");

    assertThat(clone.next).isNotSameAs(original.next);
    assertThat(clone.next.name).isEqualTo("B");
  }

  @Test
  @DisplayName("Não deve estourar a pilha (StackOverflow) em referências circulares")
  void shouldHandleCircularReferencesSafely() {
    Node a = new Node("A");
    Node b = new Node("B");
    a.next = b;
    b.next = a; // Ciclo infinito: A -> B -> A -> B...

    Node cloneA = DeepCopy.of(a);

    assertThat(cloneA.name).isEqualTo("A");
    assertThat(cloneA.next.name).isEqualTo("B");
    // Verifica se a estrutura circular foi mantida no clone
    assertThat(cloneA.next.next).isSameAs(cloneA);
  }

  @Test
  @DisplayName("Deve clonar classe sem construtor vazio usando fallback de alocação")
  void shouldCloneTypeWithoutNoArgsConstructor() {
    NoDefaultCtorNode original = new NoDefaultCtorNode("ROOT");
    original.next = new NoDefaultCtorNode("CHILD");

    NoDefaultCtorNode clone = DeepCopy.of(original);

    assertThat(clone).isNotSameAs(original);
    assertThat(clone.name).isEqualTo("ROOT");
    assertThat(clone.next).isNotSameAs(original.next);
    assertThat(clone.next.name).isEqualTo("CHILD");
  }
}