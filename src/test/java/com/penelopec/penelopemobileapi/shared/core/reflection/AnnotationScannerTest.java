package com.penelopec.penelopemobileapi.shared.core.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do AnnotationScanner")
class AnnotationScannerTest {

  @Retention(RetentionPolicy.RUNTIME) // Sem isso, o teste falharia
  @interface Auditable {
    String eventName();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Auditable(eventName = "USER_REGISTERED")
  @interface AuditedRegistration { }

  @Auditable(eventName = "BASE_ENTITY_CREATED")
  static class BaseEntity { }

  @AuditedRegistration
  static class AuditedUser extends BaseEntity { }

  static class User extends BaseEntity { }

  static class Order { }

  @Nested
  @DisplayName("Busca em Hierarquia de Classes")
  class FindAnnotation {

    @Test
    @DisplayName("Deve encontrar anotação presente na própria classe")
    void shouldFindAnnotationOnTargetClass() {
      Optional<Auditable> annotation = AnnotationScanner.findAnnotation(BaseEntity.class, Auditable.class);

      assertThat(annotation).isPresent();
      assertThat(annotation.get().eventName()).isEqualTo("BASE_ENTITY_CREATED");
    }

    @Test
    @DisplayName("Deve encontrar anotação na superclasse (subindo a hierarquia)")
    void shouldFindAnnotationOnSuperclass() {
      Optional<Auditable> annotation = AnnotationScanner.findAnnotation(User.class, Auditable.class);

      assertThat(annotation).isPresent();
      assertThat(annotation.get().eventName()).isEqualTo("BASE_ENTITY_CREATED");
    }

    @Test
    @DisplayName("Deve retornar vazio quando classe não possui a anotação na hierarquia")
    void shouldReturnEmptyWhenAnnotationIsAbsent() {
      Optional<Auditable> annotation = AnnotationScanner.findAnnotation(Order.class, Auditable.class);

      assertThat(annotation).isEmpty();
    }
  }

  @Nested
  @DisplayName("Busca por Meta-Anotações")
  class FindMetaAnnotation {

    @Test
    @DisplayName("Deve encontrar meta-anotação presente numa anotação da própria classe")
    void shouldFindMetaAnnotationOnTargetClass() {
      Optional<Auditable> annotation = AnnotationScanner.findMetaAnnotation(AuditedUser.class, Auditable.class);

      assertThat(annotation).isPresent();
      assertThat(annotation.get().eventName()).isEqualTo("USER_REGISTERED");
    }

    @Test
    @DisplayName("Deve encontrar meta-anotação ao subir a hierarquia da classe")
    void shouldFindMetaAnnotationOnSuperclass() {
      @AuditedRegistration
      class RegisteredBaseEntity { }

      class RegisteredUser extends RegisteredBaseEntity { }

      Optional<Auditable> annotation = AnnotationScanner.findMetaAnnotation(RegisteredUser.class, Auditable.class);

      assertThat(annotation).isPresent();
      assertThat(annotation.get().eventName()).isEqualTo("USER_REGISTERED");
    }

    @Test
    @DisplayName("Deve retornar vazio quando nenhuma anotação da hierarquia possuir a meta-anotação")
    void shouldReturnEmptyWhenMetaAnnotationIsAbsent() {
      Optional<Auditable> annotation = AnnotationScanner.findMetaAnnotation(Order.class, Auditable.class);

      assertThat(annotation).isEmpty();
    }
  }
}