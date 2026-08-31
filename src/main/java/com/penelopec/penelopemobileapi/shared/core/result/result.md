# ✅ Módulo Result (commons-core)

O pacote `result` modela resultados de operações como valores, tornando sucesso e falha parte explícita do contrato em vez de depender exclusivamente de exceções.

## 1. O Problema: Falhas Invisíveis no Tipo

Exceções para fluxos esperados escondem no tipo que uma operação pode falhar e fragmentam o tratamento em vários `try/catch`.

## 2. A Solução: Sucesso e Falha como Dados

`Result` e `Either` tornam ambos os caminhos composicionais: mapeie, recupere, transforme o erro ou reduza o resultado em um valor final.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 🎯 `Result` | Representa o resultado de uma operação com sucesso ou falha. |
| ↔️ `Either` | Armazena um de dois valores possíveis, normalmente falha ou sucesso. |
| ⭕ `Void` | Representa operações bem-sucedidas sem valor útil de retorno. |

Use `Result` e `Either` quando o consumidor precisar tratar o erro no próprio fluxo de dados.

`Result` é indicado para sucesso ou erro tipado; `Either` para dois valores legítimos; `Void.singleton()` representa sucesso sem valor.

---

## 4. Exemplos de Uso Profissional

```java
String message = result
	.map(Customer::name)
	.fold(name -> "Olá, " + name, error -> error.message());
```

### Exemplo 2: Decisão de negócio e comando sem retorno

```java
Either<DomainError, Customer> customer = customerId == null
	? Either.left(DomainError.of("CUSTOMER_ID", "Identificador obrigatório"))
	: Either.right(repository.find(customerId));
String response = customer.fold(DomainError::message, Customer::name);
Result<Void, DomainError> completed = Result.success(Void.singleton());
```

---

*Módulo desenhado para que resultados incertos permaneçam explícitos e compostos.*