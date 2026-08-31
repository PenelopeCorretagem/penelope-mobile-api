# 📚 Módulo Collections (commons-core)

O pacote `collections` reúne operações seguras e expressivas para percorrer, consultar, transformar e organizar coleções com a JDK.

## 1. O Problema: Coleções Sem Contrato

Laços repetidos para filtrar e localizar itens, índices manuais e coleções internas devolvidas diretamente escondem a intenção e permitem mutação acidental. Agregações CPU-bound feitas no `commonPool` também podem afetar tarefas não relacionadas da aplicação.

## 2. A Solução: Operações Declarativas e Imutáveis

O módulo torna a intenção explícita: filtros são reutilizáveis, grupos preservam suas chaves, páginas compõem transformações e cópias imutáveis protegem contratos de retorno. Para processamento pesado, `Aggregator` cria um `ForkJoinPool` dedicado.

* **Expressividade:** buscas, grupos e paginação substituem boilerplate imperativo.
* **Segurança:** argumentos inválidos falham na entrada e cópias publicadas podem ser imutáveis.
* **Isolamento:** cálculos paralelos não saturam o pool compartilhado da JVM.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| ⚙️ `Aggregator` | Executa agregações e operações de redução. |
| 🔎 `Filter` e `Filters` | Definem e combinam critérios de filtragem. |
| 🗂️ `Grouping` | Agrupa elementos e mapeia grupos resultantes. |
| 🔒 `ImmutableCollectionUtils` | Cria cópias imutáveis de listas e mapas. |
| 📑 `IndexedLoop`, `MapUtils`, `Page` e `SearchUtils` | Apoiam iteração indexada, mapas, paginação e buscas. |

Os métodos priorizam argumentos validados e retornos previsíveis, reduzindo mutação acidental e código repetitivo.

### Como escolher

Use `Grouping` quando a chave precisa continuar disponível após separar dados; `SearchUtils` quando a regra é encontrar uma ocorrência; e `ImmutableCollectionUtils` nas bordas de APIs. Escolha `Aggregator` apenas para transformações puras, associativas e realmente CPU-bound.

---

## 4. Exemplos de Uso Profissional

```java
Grouping<String, Order> ordersByCustomer = Grouping.of(orders, Order::customerId);
List<Order> customerOrders = ordersByCustomer.get("customer-42");

long total = Aggregator.of(invoices)
	.parallelMapReduce(4, 0L, Invoice::amountInCents, Long::sum);

return ImmutableCollectionUtils.safeImmutableList(activeProducts);
```

### Exemplo 2: Filtro, busca, paginação e mapas de uma consulta administrativa

```java
Filter<Long> positiveId = Filter.greaterThan(0L);
Filter<String> blankName = Filters.isBlank();
Page<Order> firstPage = Page.<Order>empty().filter(order -> positiveId.matches(order.id()));
boolean hasPriority = SearchUtils.containsAny(firstPage.content(), Order::priority);

Map<String, Order> byId = MapUtils.newHashMapWithExpectedSize(orders.size());
MapUtils.mergeSafe(importedOrders, byId);
IndexedLoop.forEach(orders, (index, order) -> audit.log(index + ": " + order.id()));
```

---

*Módulo desenhado para tornar o trabalho com coleções explícito, seguro e legível, sem dependências externas.*