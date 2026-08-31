package com.penelopec.penelopemobileapi.shared.core.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utilitário para mesclar objetos (Partial Update / PATCH).
 */
public final class Merge {

  private Merge() { throw new UnsupportedOperationException(); }

  /**
   * Copia todos os campos não-nulos do objeto de origem para o objeto de destino.
   * Ignora campos declarados como final.
   *
   * @param source Objeto contendo os novos valores.
   * @param target Objeto que será atualizado.
   * @param <T> O tipo dos objetos.
   */
  public static <T> void updateNonNull(T source, T target) {
    updateNonNull(source, target, List.of());
  }

  /**
   * Copia todos os campos não-nulos do objeto de origem para o objeto de destino,
   * ignorando os campos cujo nome esteja na lista de propriedades ignoradas.
   *
   * @param source objeto contendo os novos valores
   * @param target objeto que será atualizado
   * @param ignoredProperties nomes de campos que não devem ser sobrescritos
   */
  public static void updateNonNull(Object source, Object target, List<String> ignoredProperties) {
    Objects.requireNonNull(source, "Source não pode ser nulo");
    Objects.requireNonNull(target, "Target não pode ser nulo");
    Objects.requireNonNull(ignoredProperties, "ignoredProperties não pode ser nulo");

    if (!target.getClass().isAssignableFrom(source.getClass())
      && !source.getClass().isAssignableFrom(target.getClass())) {
      throw new IllegalArgumentException("Source e Target devem ser compatíveis na hierarquia de tipos");
    }

    Set<String> ignored = new HashSet<>(ignoredProperties);

    Class<?> clazz = source.getClass();
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
          continue;
        }

        if (ignored.contains(field.getName())) {
          continue;
        }

        boolean originalAccess = field.canAccess(source);
        try {
          if (!originalAccess) field.setAccessible(true);

          Object value = field.get(source);
          if (value != null) {
            field.set(target, value);
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Falha ao mesclar campo: " + field.getName(), e);
        } finally {
          if (!originalAccess) field.setAccessible(false);
        }
      }
      clazz = clazz.getSuperclass();
    }
  }
}