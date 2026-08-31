package com.penelopec.penelopemobileapi.shared.core.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
@Nested
@DisplayName("Testes do TypeToken")
class TypeTokenTest {

  @Test
  @DisplayName("Deve capturar o tipo parametrizado de coleções")
  void shouldCaptureParameterizedTypeWhenAnonymousClassCreated() {
    TypeToken<List<String>> token = new TypeToken<>() {};
    Type type = token.getType();

    assertThat(type).isInstanceOf(ParameterizedType.class);
    ParameterizedType parameterizedType = (ParameterizedType) type;
    assertThat(parameterizedType.getRawType()).isEqualTo(List.class);
    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
  }

  @Test
  @DisplayName("Deve retornar raw type de tipo parametrizado")
  void shouldReturnRawTypeForParameterizedType() {
    TypeToken<List<String>> token = new TypeToken<>() { };

    assertThat(token.getRawType()).isEqualTo(List.class);
  }

  @Test
  @DisplayName("Deve retornar a própria classe quando o tipo capturado for simples")
  void shouldReturnClassWhenCapturedTypeIsClass() {
    TypeToken<String> token = new TypeToken<>() { };

    assertThat(token.getRawType()).isEqualTo(String.class);
  }

  @Test
  @DisplayName("Deve falhar rápido se não for subclasse anônima")
  @SuppressWarnings("rawtypes")
  void shouldFailWhenInstantiatedDirectly() {
    // Simulando uma implementação de classe errada em vez de subclasse anônima
    class BadTypeToken extends TypeToken {}

    assertThatThrownBy(BadTypeToken::new)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("TypeToken deve ser instanciado através de uma subclasse anônima");
  }
}