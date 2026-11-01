# 🧩 Módulo Object (commons-core)

O pacote `object` fornece blocos de composição para construção, equivalência, wrapping, imutabilidade e casting seguro de objetos.

## 1. O Problema: Contratos Repetidos

Builders, wrappers e comparações costumam repetir validações e `equals` frágeis. Casts diretos espalham `ClassCastException` pelo fluxo.

## 2. A Solução: Composição Segura

O módulo concentra esses contratos: builders podem retornar `Result`, equivalências são explícitas e casts retornam `Optional`.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 🏗️ `Builder` e `AbstractBuilder` | Definem contratos e suporte para construção de objetos. |
| ⚖️ `Equivalence`, `Equivalences` e `AbstractEquivalence` | Modelam critérios de equivalência além de `equals`. |
| 🎁 `Wrapper`, `Wrappers` e `AbstractWrapper` | Padronizam objetos que encapsulam valores. |
| 🔎 `Cast`, `ImmutableValue` e `ObjectUtils` | Oferecem casting seguro, valores imutáveis e utilitários. |
| 🔄 `CyclicReferenceException` | Sinaliza ciclos encontrados em operações sobre objetos. |

Os contratos privilegiam composição e validação imediata para reduzir estados inválidos.

`AbstractBuilder` fornece construção fluente e segura; `Equivalences` define critérios como identidade e texto sem considerar maiúsculas; `Wrapper` e `ImmutableValue` tornam encapsulamento explícito.

---

## 4. Exemplos de Uso Profissional

```java
Optional<AdminUser> admin = Cast.as(user, AdminUser.class);
ImmutableValue<String> name = ImmutableValue.of(rawName).map(String::trim);
boolean same = Equivalences.ignoreCase().equivalent("Java", "JAVA");
```

### Exemplo 2: Construção, equivalência e composição de um agregado

```java
final class OrderBuilder extends AbstractBuilder<Order, OrderBuilder> {
	protected OrderBuilder self() { return this; }
	protected Order doBuild() { return new Order(id); }
}
Builder<Order> builder = new OrderBuilder().withId("order-42");
Order order = builder.build();

Equivalence<Order> byId = new AbstractEquivalence<>() {
	protected boolean doEquivalent(Order left, Order right) { return left.id().equals(right.id()); }
	protected int doHash(Order value) { return value.id().hashCode(); }
};
Wrapper<Order> wrapped = Wrappers.of(order);
Order root = Wrappers.getRoot(wrapped);
boolean unchanged = ObjectUtils.deepEquals(order, root);
```

### Exemplo 3: Decorador seguro e detecção de composição inválida

```java
final class AuditedOrder extends AbstractWrapper<Order> {
	AuditedOrder(Order delegate) { super(delegate); }
}
try {
	new AuditedOrder(order).unwrap(Order.class);
} catch (CyclicReferenceException exception) {
	audit.warn(exception.getMessage());
}
```

---

*Módulo desenhado para compor objetos com contratos claros e sem casting inseguro.*