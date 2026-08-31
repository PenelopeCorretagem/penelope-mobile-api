package com.penelopec.penelopemobileapi.shared.core.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do BeanUtils")
class BeanUtilsTest {

  static class BaseEntity {
    private String id;
  }

  static class User extends BaseEntity {
    private final String name;

    User(String name) {
      this.name = name;
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<Object, Object> fieldCache() throws Exception {
    Field cacheField = BeanUtils.class.getDeclaredField("FIELD_CACHE");
    cacheField.setAccessible(true);
    return (Map<Object, Object>) cacheField.get(null);
  }

  private static void clearFieldCache() throws Exception {
    fieldCache().clear();
  }

  @Nested
  @DisplayName("Leitura de Propriedades")
  class ReadProperty {

    @Test
    @DisplayName("Deve reutilizar o cache para leituras repetidas da mesma propriedade")
    void shouldReuseCacheForRepeatedReads() throws Exception {
      clearFieldCache();
      User user = new User("Alice");

      BeanUtils.readProperty(user, "name");
      int cacheSizeAfterFirstRead = fieldCache().size();

      BeanUtils.readProperty(user, "name");

      assertThat(cacheSizeAfterFirstRead).isEqualTo(1);
      assertThat(fieldCache()).hasSize(1);
    }

    @Test
    @DisplayName("Deve cachear também a resolução de campo herdado")
    void shouldCacheInheritedFieldResolution() throws Exception {
      clearFieldCache();
      User user = new User("Bob");
      BeanUtils.writeProperty(user, "id", "UUID-123");

      BeanUtils.readProperty(user, "id");
      int cacheSizeAfterFirstRead = fieldCache().size();

      BeanUtils.readProperty(user, "id");

      assertThat(cacheSizeAfterFirstRead).isEqualTo(1);
      assertThat(fieldCache()).hasSize(1);
    }

    @Test
    @DisplayName("Deve ler campo privado da própria classe")
    void shouldReadPrivateFieldWhenInSameClass() {
      User user = new User("Alice");
      String name = BeanUtils.readProperty(user, "name");
      assertThat(name).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Deve ler campo privado da superclasse")
    void shouldReadPrivateFieldWhenInSuperclass() {
      User user = new User("Bob");
      BeanUtils.writeProperty(user, "id", "UUID-123"); // Usando nossa própria utilitária para preparar o teste

      String id = BeanUtils.readProperty(user, "id");
      assertThat(id).isEqualTo("UUID-123");
    }

    @Test
    @DisplayName("Deve falhar rápido ao buscar campo inexistente")
    void shouldThrowExceptionWhenFieldDoesNotExist() {
      User user = new User("Charlie");
      assertThatThrownBy(() -> BeanUtils.readProperty(user, "invalidField"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Campo não encontrado");
    }
  }
}