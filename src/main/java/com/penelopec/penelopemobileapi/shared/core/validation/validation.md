# 🧾 Módulo Validation (commons-core)

O pacote `validation` fornece contratos funcionais para validar dados e devolver violações estruturadas, sem acoplamento a frameworks de validação.

## 1. O Problema: Validação Acoplada e Opaca

Quando a regra valida e decide a resposta no mesmo ponto, ela perde reuso e testes isolados. Mensagens soltas também tornam difícil apresentar todos os problemas ao usuário.

## 2. A Solução: Regras e Resultado Separados

Validadores produzem resultados estruturados, com violações que a camada consumidora pode apresentar, registrar ou converter em resposta de API.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| ✅ `Validator` | Define o contrato para validadores de um tipo. |
| 📋 `ValidationResult` | Representa o resultado consolidado de uma validação. |
| 📌 `ConstraintResult` | Modela o resultado individual de uma restrição. |
| 🚫 `Violations` | Armazena e organiza violações encontradas. |

O resultado permite que a camada consumidora decida como apresentar ou processar os erros, preservando regras livres de efeitos colaterais.

`Validator` é o contrato funcional; `ConstraintResult` identifica campo e mensagem; `Violations` agrupa mensagens; `ValidationResult` indica validade e cria resultados básicos.

---

## 4. Exemplos de Uso Profissional

```java
ValidationResult result = validator.validate(request);
if (!result.isValid()) {
	return Response.badRequest().entity(result.violations()).build();
}
```

### Exemplo 2: Erro de campo convertido em resposta de API

```java
Validator<CustomerRequest> requiredName = request -> request.name().isBlank()
	? ValidationResult.invalid("Nome obrigatório")
	: ValidationResult.valid();
ConstraintResult constraint = new ConstraintResult("name", "Nome obrigatório");
Violations violations = Violations.of(constraint.message());
return requiredName.validate(request).isValid() ? accepted() : badRequest(violations);
```

---

*Módulo desenhado para validação testável, reutilizável e independente de frameworks.*