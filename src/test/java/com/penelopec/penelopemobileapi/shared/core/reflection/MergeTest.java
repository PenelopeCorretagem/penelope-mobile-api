package com.penelopec.penelopemobileapi.shared.core.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

  @Nested
  @DisplayName("Merge")
  class MergeTest {

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

    @Test
    @DisplayName("Deve ignorar campos nulos na origem e atualizar apenas os preenchidos")
    void shouldMergeOnlyNonNullFields() {
      Dto target = new Dto();
      target.title = "Original";
      target.age = 30;

      Dto source = new Dto();
      source.title = "Atualizado";
      // source.age é null

      Merge.updateNonNull(source, target);

      assertThat(target.title).isEqualTo("Atualizado");
      assertThat(target.age).isEqualTo(30); // Mantido o original
    }

    @Test
    @DisplayName("Deve ignorar campos explicitamente listados em ignoredProperties")
    void shouldIgnoreFieldsFromIgnoredPropertiesList() {
      Dto target = new Dto();
      target.title = "Original";
      target.age = 30;

      Dto source = new Dto();
      source.title = "Atualizado";
      source.age = 50;

      Merge.updateNonNull(source, target, List.of("age"));

      assertThat(target.title).isEqualTo("Atualizado");
      assertThat(target.age).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve falhar rápido quando ignoredProperties for nula")
    void shouldFailFastWhenIgnoredPropertiesIsNull() {
      Dto source = new Dto();
      Dto target = new Dto();

      assertThatThrownBy(() -> Merge.updateNonNull(source, target, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ignoredProperties não pode ser nulo");
    }
  }