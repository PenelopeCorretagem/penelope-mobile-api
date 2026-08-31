# 🛑 Módulo Exception (commons-core)

O pacote `exception` estabelece uma hierarquia de falhas com códigos e categorias claras para que consumidores tratem erros sem depender de mensagens textuais.

## 1. O Problema: Exceções Genéricas e Mensagens Frágeis

Quando todo erro é uma exceção genérica com texto livre, consumidores precisam interpretar mensagens para definir respostas, logs e retentativas. Isso duplica regras e mistura falhas de negócio com indisponibilidade técnica.

## 2. A Solução: Falhas Tipadas com Semântica

`CoreException` é a base selada de falhas de domínio e transporta sempre um `DomainError`. As subclasses indicam por `isRetryable()` se uma nova execução é aceitável; o tipo da exceção identifica a categoria do problema.

* **Consistência:** códigos estruturados substituem textos frágeis.
* **Intenção:** validação, negócio e recurso ausente possuem tipos próprios.
* **Eficiência:** falhas esperadas de domínio não montam stack trace.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 🧱 `CoreException` | Base das exceções da biblioteca. |
| 📋 `BusinessException`, `ValidationException` e `NotFoundException` | Representam falhas esperadas de regras e dados. |
| 🔧 `InfrastructureException` | Representa falhas em recursos técnicos. |
| ⏱️ `AsyncExecutionException`, `AsyncTimeoutException` e `RetryExhaustedException` | Descrevem falhas de concorrência. |
| 🏷️ `ErrorCode`, `CoreErrorCode` e `DomainError` | Modelam códigos e detalhes estruturados. |

A hierarquia permite recuperar, informar ou propagar uma falha de acordo com sua categoria.

### Como escolher

Use as exceções de domínio para situações previstas pelo contrato. Para falhas técnicas, preserve a causa e use a exceção de infraestrutura ou concorrência apropriada. Nunca decida uma retentativa apenas pelo texto da mensagem: consulte `isRetryable()`.

---

## 4. Exemplos de Uso Profissional

```java
try {
	return service.findOrder(orderId);
} catch (NotFoundException exception) {
	return Response.status(404).entity(exception.domainError()).build();
}

catch (CoreException exception) {
	if (exception.isRetryable()) {
		scheduleRetry();
	}
	throw exception;
}
```

### Exemplo 2: Classificação de falhas por código e categoria

```java
ErrorCode errorCode = CoreErrorCode.values()[0];
DomainError error = DomainError.of(errorCode.code(), errorCode.defaultMessage());
throw new ValidationException(error);
throw new InfrastructureException("Serviço de crédito indisponível", cause);
throw new AsyncExecutionException("Execução assíncrona falhou", cause);
throw new AsyncTimeoutException("Consulta excedeu o prazo");
throw new RetryExhaustedException("Tentativas esgotadas", cause);
throw new BusinessException(error);
```

---

*Módulo desenhado para que falhas sejam dados compreensíveis e tratáveis, não apenas mensagens soltas.*