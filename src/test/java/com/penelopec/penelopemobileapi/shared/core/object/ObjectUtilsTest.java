package com.penelopec.penelopemobileapi.shared.core.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ObjectUtils")
class ObjectUtilsTest {

  @Nested
  @DisplayName("Método deepEquals")
  class DeepEquals {

    @Test
    @DisplayName("Deve retornar true quando os objetos forem a mesma instância")
    void shouldReturnTrueWhenObjectsAreSameInstance() {
      String obj = "test";
      assertThat(ObjectUtils.deepEquals(obj, obj)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true para ambos nulos e false quando apenas um for nulo")
    void shouldHandleNullsSafely() {
      assertThat(ObjectUtils.deepEquals(null, null)).isTrue();
      assertThat(ObjectUtils.deepEquals("a", null)).isFalse();
      assertThat(ObjectUtils.deepEquals(null, "b")).isFalse();
    }

    @Test
    @DisplayName("Deve comparar arrays estruturalmente, resolvendo o problema de ponteiros")
    void shouldCompareArraysStructurally() {
      String[] arr1 = {"a", "b", "c"};
      String[] arr2 = {"a", "b", "c"};
      String[] arr3 = {"a", "b", "d"};

      assertThat(ObjectUtils.deepEquals(arr1, arr2)).isTrue();
      assertThat(ObjectUtils.deepEquals(arr1, arr3)).isFalse();
    }
  }

  @Nested
  @DisplayName("Método requireImmutable")
  class RequireImmutable {

    static class ImmutableClass {
      private final String id = "1";
      public final int value = 100;
    }

    static class NonImmutableClass {
      private final String id = "1";
      private int value; // Infrator
    }

    static class EmptyClass {}

    record ValueObjectRecord(String id, String name) {}

    @Test
    @DisplayName("Deve passar sem lançar exceções para classes totalmente imutáveis")
    void shouldNotThrowExceptionForFullyImmutableClass() {
      assertThatCode(() -> ObjectUtils.requireImmutable(ImmutableClass.class))
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve passar sem lançar exceções para Java Records")
    void shouldNotThrowExceptionForJavaRecords() {
      assertThatCode(() -> ObjectUtils.requireImmutable(ValueObjectRecord.class))
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve passar sem lançar exceções para classes vazias")
    void shouldNotThrowExceptionForEmptyClass() {
      assertThatCode(() -> ObjectUtils.requireImmutable(EmptyClass.class))
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException contendo os nomes dos atributos mutáveis")
    void shouldThrowExceptionWhenClassHasMutableFields() {
      assertThatThrownBy(() -> ObjectUtils.requireImmutable(NonImmutableClass.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("A classe")
        .hasMessageContaining("não é estritamente imutável")
        .hasMessageContaining("O atributo 'value' deve ser declarado como final");
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se a classe fornecida for nula")
    void shouldThrowExceptionWhenClassIsNull() {
      assertThatThrownBy(() -> ObjectUtils.requireImmutable(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A classe fornecida não pode ser nula.");
    }
  }
}