package com.penelopec.penelopemobileapi.shared.core.collections;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Representa uma fatia (página) de dados numa coleção maior.
 * Imutável e segura contra modificações da lista original (Defensive Copy).
 *
 * @param <T> O tipo de dado contido na página.
 */
public record Page<T>(
  List<T> content,
  int pageNumber,
  int pageSize,
  long totalElements
) {
  public Page {
    Objects.requireNonNull(content, "O conteúdo da página não pode ser nulo.");
    // Cópia defensiva: garante a imutabilidade profunda da casca (a lista)
    content = List.copyOf(content);

    if (pageNumber < 0) {
      throw new IllegalArgumentException("O número da página não pode ser negativo.");
    }
    if (pageSize < 1) {
      throw new IllegalArgumentException("O tamanho da página deve ser maior que zero.");
    }
    if (totalElements < 0) {
      throw new IllegalArgumentException("O total de elementos não pode ser negativo.");
    }
  }

  /**
   * Retorna uma página vazia tipada com segurança via Type Inference.
   */
  public static <T> Page<T> empty() {
    return new Page<>(List.of(), 0, 1, 0L);
  }

  /**
   * Aplica uma transformação a todos os elementos desta página, mantendo a estrutura
   * de paginação intacta. Utiliza covariância/contravariância para flexibilidade funcional.
   *
   * @param mapper A função de transformação.
   * @param <U>    O tipo de retorno após a transformação.
   * @return Uma nova página contendo os elementos transformados.
   */
  public <U> Page<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper, "A função de mapeamento não pode ser nula.");

    List<U> mappedContent = content.stream()
      .<U>map(mapper)
      .toList();

    return new Page<>(mappedContent, pageNumber, pageSize, totalElements);
  }

  /**
   * Filtra os elementos desta página com base num predicado.
   * * <p>Retorna uma nova página imutável. Por simplicidade, caso a filtragem ocorra
   * na página inicial (pageNumber == 0), o total de elementos é recalculado
   * subtraindo a quantidade de itens removidos pelo filtro. Nas demais páginas,
   * o total de elementos original é preservado.</p>
   *
   * @param predicate A condição que os elementos devem satisfazer para serem mantidos.
   * @return Uma nova página contendo os elementos filtrados.
   */
  public Page<T> filter(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate, "A função de predicado não pode ser nula.");

    List<T> filteredContent = content.stream()
      .filter(predicate)
      .toList();

    long removedElementsCount = content.size() - filteredContent.size();

    // Recalcula o total apenas se for a página inicial
    long newTotalElements = (pageNumber == 0)
      ? Math.max(0, totalElements - removedElementsCount)
      : totalElements;

    return new Page<>(filteredContent, pageNumber, pageSize, newTotalElements);
  }

  /**
   * Filtra os elementos desta página com base no contrato de domínio Filter<T>.
   *
   * <p>Retorna uma nova página imutável recalculando o total de elementos
   * apenas se estivermos na página inicial.</p>
   *
   * @param filter O componente de filtragem de domínio.
   * @return Uma nova página contendo os elementos filtrados.
   */
  public Page<T> filter(Filter<? super T> filter) {
    Objects.requireNonNull(filter, "O filtro não pode ser nulo.");

    // ADAPTADOR IMPLÍCITO: filter::matches assina perfeitamente
    // o contrato que o .filter() do Stream (Predicate) exige.
    List<T> filteredContent = content.stream()
      .filter(filter::matches)
      .toList();

    long removedElementsCount = content.size() - filteredContent.size();

    long newTotalElements = (pageNumber == 0)
      ? Math.max(0, totalElements - removedElementsCount)
      : totalElements;

    return new Page<>(filteredContent, pageNumber, pageSize, newTotalElements);
  }
}