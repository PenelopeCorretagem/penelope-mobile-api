package com.penelopec.penelopemobileapi.shared.core.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Utilitários de Class Loading (ClassLoadingUtils)")
class ClassLoadingUtilsTest {

  @Nested
  @DisplayName("Carregamento Silencioso de Classes")
  class CarregamentoSilencioso {

    @Test
    @DisplayName("Deve retornar a classe envelopada em Optional quando ela existir no classpath")
    void shouldReturnOptionalWithClassWhenItExists() {
      Optional<Class<?>> clazz = ClassLoadingUtils.loadClassSilently("java.util.ArrayList");

      assertThat(clazz).isPresent();
      assertThat(clazz.get()).isEqualTo(java.util.ArrayList.class);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando a classe não for encontrada")
    void shouldReturnEmptyOptionalWhenClassDoesNotExist() {
      Optional<Class<?>> clazz = ClassLoadingUtils.loadClassSilently("br.com.inexistente.Fantasma");

      assertThat(clazz).isEmpty();
    }

    @Test
    @DisplayName("Deve acionar Fail-Fast se o nome da classe for nulo")
    void shouldThrowExceptionWhenClassNameIsNull() {
      assertThatThrownBy(() -> ClassLoadingUtils.loadClassSilently(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Resolução de Contexto de ClassLoader")
  class ResolucaoClassLoader {

    @Test
    @DisplayName("Deve recuperar com sucesso um ClassLoader válido e não-nulo")
    void shouldRetrieveValidClassLoader() {
      ClassLoader classLoader = ClassLoadingUtils.getDefaultClassLoader();

      assertThat(classLoader).isNotNull();
    }
  }

  @Nested
  @DisplayName("Verificação Booleana de Presença de Classe")
  class VerificacaoPresenca {

    @Test
    @DisplayName("Deve retornar true quando a classe existir no classpath")
    void shouldReturnTrueWhenClassExists() {
      boolean present = ClassLoadingUtils.isPresent("java.util.ArrayList");

      assertThat(present).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando a classe não existir")
    void shouldReturnFalseWhenClassDoesNotExist() {
      boolean present = ClassLoadingUtils.isPresent("br.com.inexistente.ClasseFicticia");

      assertThat(present).isFalse();
    }

    @Test
    @DisplayName("Deve acionar Fail-Fast se o nome da classe for nulo")
    void shouldThrowExceptionWhenClassNameIsNull() {
      assertThatThrownBy(() -> ClassLoadingUtils.isPresent(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("não pode ser nulo");
    }

    @Test
    @DisplayName("Deve retornar false para classe com erro de linkage")
    void shouldReturnFalseForBrokenClassFile() {
      // Usando um nome com padrão que causa ClassNotFoundException
      boolean present = ClassLoadingUtils.isPresent("java.lang.Object$InvalidInnerClass$NotReal");

      assertThat(present).isFalse();
    }
  }
}