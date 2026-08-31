package com.penelopec.penelopemobileapi.shared.core.reflection;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BeanUtils {

  private static final ConcurrentHashMap<FieldCacheKey, Optional<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

  private  BeanUtils() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado");
  }

  /**
   * Lê o valor de um campo privado de um objeto, navegando pela hierarquia de herança se necessário.
   *
   * @param obj       O objeto alvo.
   * @param fieldName O nome do campo.
   * @param <T>       O tipo esperado de retorno.
   * @return O valor do campo.
   * @throws IllegalArgumentException se o objeto ou nome do campo forem nulos.
   * @throws RuntimeException se o campo não existir ou não puder ser acessado.
   */
  @SuppressWarnings("unchecked")
  public static <T> T readProperty(Object obj, String fieldName) {
    Objects.requireNonNull(obj, "Objeto alvo não pode ser nulo");
    Objects.requireNonNull(fieldName, "Nome do campo não pode ser nulo");

    Field field = findField(obj.getClass(), fieldName);

    boolean originalAccess = field.canAccess(obj);

    try {
      if (!originalAccess) {
        field.setAccessible(true);
      }
      return (T) field.get(obj);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Falha de segurança ao acessar campo: " + fieldName, e);
    } finally {
      if (!originalAccess) {
        field.setAccessible(false); // Restaura o estado para evitar vazamento de privilégios
      }
    }
  }

  /**
   * Escreve um valor em um campo privado de um objeto, navegando pela hierarquia de herança.
   */
  public static void writeProperty(Object obj, String fieldName, Object value) {
    Objects.requireNonNull(obj, "Objeto alvo não pode ser nulo");
    Objects.requireNonNull(fieldName, "Nome do campo não pode ser nulo");

    Field field = findField(obj.getClass(), fieldName);
    boolean originalAccess = field.canAccess(obj);

    try {
      if (!originalAccess) {
        field.setAccessible(true);
      }
      field.set(obj, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Falha de segurança ao escrever no campo: " + fieldName, e);
    } finally {
      if (!originalAccess) {
        field.setAccessible(false);
      }
    }
  }

  private static Field findField(Class<?> clazz, String fieldName) {
    FieldCacheKey cacheKey = new FieldCacheKey(clazz, fieldName);

    return FIELD_CACHE.computeIfAbsent(cacheKey, key -> findFieldInHierarchy(key.ownerType(), key.fieldName()))
      .orElseThrow(() -> new RuntimeException("Campo não encontrado na hierarquia da classe: " + fieldName));
  }

  private static Optional<Field> findFieldInHierarchy(Class<?> clazz, String fieldName) {
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      try {
        return Optional.of(current.getDeclaredField(fieldName));
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }

    return Optional.empty();
  }

  private record FieldCacheKey(Class<?> ownerType, String fieldName) {
  }
}
