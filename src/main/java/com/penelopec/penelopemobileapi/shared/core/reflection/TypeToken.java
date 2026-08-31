package com.penelopec.penelopemobileapi.shared.core.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Super Type Token para capturar tipos genéricos em tempo de execução
 * contornando a limitação do Type Erasure.
 */
public abstract class TypeToken<T> {

  private final Type type;

  protected TypeToken() {
    Type superclass = getClass().getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new IllegalStateException("TypeToken deve ser instanciado através de uma subclasse anônima (com chaves {}).");
    }
    this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
  }

  public Type getType() {
    return type;
  }

  /**
   * Retorna o tipo bruto (raw type) do token capturado.
   *
   * @return classe bruta associada ao tipo capturado
   * @throws IllegalStateException quando o tipo capturado não puder ser reduzido a Class
   */
  public Class<?> getRawType() {
    if (type instanceof Class<?> clazz) {
      return clazz;
    }

    if (type instanceof ParameterizedType parameterizedType) {
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?> clazz) {
        return clazz;
      }
    }

    throw new IllegalStateException("Não foi possível determinar o raw type para: " + type);
  }
}

