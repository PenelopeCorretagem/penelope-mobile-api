# 🔍 Módulo Reflection (commons-core)

O pacote `reflection` encapsula recursos de reflexão para inspeção, cópia e composição de objetos sem espalhar detalhes de `java.lang.reflect` pela aplicação.

## 1. O Problema: Reflexão Espalhada

Código reflexivo repetido é verboso, frágil e tende a ocultar falhas de acesso ou tipos. Sem uma fronteira, detalhes de `java.lang.reflect` contaminam regras de negócio.

## 2. A Solução: Flexibilidade Encapsulada

As operações ficam em APIs específicas para anotações, propriedades, cópia, merge, proxies e tipos genéricos.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 🏷️ `AnnotationScanner` | Localiza e consulta anotações em tipos e membros. |
| 🧾 `BeanUtils` | Oferece operações reflexivas sobre propriedades de beans. |
| 🧬 `DeepCopy` | Cria cópias profundas de grafos de objetos. |
| 🪞 `DynamicProxy` | Cria proxies dinâmicos e listas não modificáveis. |
| 🔗 `Merge` e `TypeToken` | Combinam objetos e preservam tipos genéricos em runtime. |

Reflexão amplia a flexibilidade, mas deve ser aplicada deliberadamente por seu custo e por depender de tipos disponíveis em runtime.

`BeanUtils` lê e escreve propriedades; `Merge` atualiza apenas valores não nulos; `DynamicProxy` intercepta interfaces e oferece listas de leitura; `TypeToken` preserva tipo genérico em runtime.

---

## 4. Exemplos de Uso Profissional

```java
Merge.updateNonNull(updateRequest, existingCustomer);
List<String> view = DynamicProxy.unmodifiableList(names);
Optional<Role> role = AnnotationScanner.findAnnotation(User.class, Role.class);
```

### Exemplo 2: Mapeamento genérico e atualização controlada

```java
Customer snapshot = DeepCopy.of(existingCustomer);
BeanUtils.writeProperty(snapshot, "status", Status.ACTIVE);
String status = BeanUtils.readProperty(snapshot, "status");
Type type = new TypeToken<List<Customer>>() {}.getType();
audit.record(type, status);
```

---

*Módulo desenhado para isolar o poder e o custo da reflexão em contratos reutilizáveis.*