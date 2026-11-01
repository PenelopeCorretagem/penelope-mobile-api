# ⚡ Módulo Concurrency (commons-core)

O pacote `concurrency` disponibiliza controles pequenos e independentes para executar tarefas assíncronas com políticas explícitas de espera, repetição e limitação.

## 1. O Problema: Concorrência Sem Limites

Criar threads para cada tarefa ou usar o `commonPool` sem critério provoca saturação e latência imprevisível. Repetir chamadas indefinidamente, ignorar timeout ou aceitar tráfego ilimitado transforma falhas transitórias em indisponibilidade.

## 2. A Solução: Políticas Reutilizáveis

Cada tipo representa uma decisão operacional. `AsyncHelper` compartilha um executor limitado com backpressure, enquanto timeout, repetição e taxa de acesso são declarados junto da operação.

* **Backpressure:** fila limitada e política `CallerRuns` impedem acúmulo ilimitado.
* **Recuperação:** retry é limitado por tentativas e atraso.
* **Proteção:** limite de taxa pode ser global ou por cliente.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 🚀 `AsyncHelper` | Simplifica a execução e composição de operações assíncronas. |
| 🚦 `RateLimiter` | Controla a taxa de execução de operações. |
| 🔁 `RetryPolicy` | Define tentativas e condições de repetição. |
| ⏱️ `TimeoutWrapper` | Aplica limite de tempo a operações. |

Use estes recursos para declarar a política de execução junto ao fluxo de negócio, sem bibliotecas externas em runtime.

### Como escolher

Use `AsyncHelper.supply` para tarefas com resultado e `run` para efeitos assíncronos. Aplique `RetryPolicy` somente a falhas transitórias e idempotentes. `TimeoutWrapper` define o comportamento após o prazo; `RateLimiter` deve proteger recursos finitos antes do trabalho começar.

---

## 4. Exemplos de Uso Profissional

```java
CompletableFuture<Customer> future = AsyncHelper.supply(
		() -> customerRepository.findById(customerId));

RetryPolicy retry = RetryPolicy.of(3, Duration.ofSeconds(1));
PaymentResponse response = retry.execute(() -> paymentGateway.charge(request));

if (!rateLimiter.tryAcquire(clientId)) {
	throw new IllegalStateException("Limite de requisições excedido");
}
```

### Exemplo 2: Catálogo com prazo e resposta degradada

```java
CompletableFuture<Catalog> catalog = TimeoutWrapper.executeWithFallback(
	AsyncHelper.supply(catalogClient::fetch),
	Duration.ofSeconds(2),
	Catalog::empty);
```

---

*Módulo desenhado para tornar concorrência uma política observável, limitada e testável.*