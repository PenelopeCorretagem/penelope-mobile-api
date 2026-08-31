# ⚙️ Módulo Functional (commons-core)

O pacote `functional` fornece uma fundação de utilitários declarativos para a criação de fluxos de dados, roteamento de regras e transformação de objetos. Ele traz os conceitos da programação funcional matemática para o ecossistema enterprise do Java, garantindo código limpo, testável e livre de mutação de estado.

## 1. O Problema: O Caos Imperativo

No dia a dia do desenvolvimento corporativo, é comum encontrarmos códigos com os seguintes problemas:

* **Aninhamento Profundo (Matrioska):** Chamadas como `salvar(formatar(limpar(dado)))` que invertem a ordem natural de leitura.
* **Variáveis Temporárias Mutações:** O uso excessivo de variáveis intermediárias (`dadoLimpo`, `dadoFormatado`) apenas para repassar estados.
* **If/Else e Switch-Cases Gigantes:** Lógicas de roteamento acopladas que dificultam a adição de novos comportamentos sem modificar o código existente (ferindo o Open/Closed Principle).
* **Complexidade de Injeção em Lambdas:** Dificuldade de usar métodos que exigem múltiplos parâmetros dentro de APIs modernas como `Stream` ou pipelines.

## 2. A Solução: Composição Funcional

Este módulo resolve essas dores tratando comportamentos como cidadãos de primeira classe. Ao invés de dizer *como* o computador deve processar os dados passo a passo (imperativo), nós declaramos um fluxo (declarativo) utilizando pequenos blocos de montar.

Todos os componentes seguem as premissas da biblioteca:

* **Zero Fricção:** Sem anotações complexas, sem dependências de frameworks (Spring/Hibernate).
* **Fail-Fast Nativo:** Validação estrita de nulidade logo na entrada de cada método.
* **Imutabilidade:** Nossas estruturas (como o `Pipeline`) são seguras para uso em ambientes *Multithread*.

---

## 3. Guia Rápido dos Componentes

### 🔄 `Transformer<I, O>` e `BiTransformer<I1, I2, O>`

Contratos semânticos para funções puras de transformação. Substituem as genéricas `Function` e `BiFunction` para dar peso de Domínio às operações de conversão de dados. Possuem métodos nativos para encadeamento (`andThen`).

### 🛤️ `Pipeline<T>`

Implementa o padrão arquitetural *Pipes and Filters*. Permite compor múltiplos `Transformer`s de forma imutável e encadeada. O fluxo de dados é lido fluentemente da esquerda para a direita (ou de cima para baixo).

### 🚦 `RuleEngine<T>`

Motor de avaliação para múltiplos predicados (validações). Garante execução *Fail-Fast* (para na primeira falha) sem a necessidade de aninhar dezenas de blocos `if`.

### 🔀 `Strategy<I, O>`

Substitui estruturas clássicas e complexas do padrão Strategy OO e blocos `switch-case`. Roteia um dado de entrada para a ação correspondente baseando-se na primeira condição que retornar verdadeiro.

### 🍛 `Curry`

Utilitário matemático para *Aplicação Parcial*. Permite "congelar" um ou mais argumentos de um `BiTransformer`, transformando-o em um `Transformer` de parâmetro único. Essencial para injetar dependências (como conexões de banco) em métodos que serão usados dentro de pipelines ou streams puros.

---

## 4. Exemplos de Uso Profissional

Abaixo, veja como aplicar esses conceitos no dia a dia para refatorar código legado.

### Exemplo 1: Limpeza e Transformação de Dados com `Pipeline`

**O Cenário:** Você recebe um payload de texto sujo, precisa remover espaços, formatar e construir um objeto de Domínio.

```java
// O Pipeline é instanciado uma única vez (thread-safe)
private static final Pipeline<String> NORMALIZE_PIPELINE = Pipeline.<String>create()
        .addStep(String::trim)
        .addStep(String::toLowerCase)
        .addStep(TextUtils::removeAccents);

public UserDomain processInput(String rawText) {
    // Código declarativo, claro e livre de estados intermediários
    String cleanText = NORMALIZE_PIPELINE.execute(rawText);
    return new UserDomain(cleanText);
}

```

### Exemplo 2: Validação Fluente com `RuleEngine`

**O Cenário:** Aprovar uma transação verificando múltiplas regras sem criar um "God Method".

```java
public boolean isAuthorized(Transaction tx) {
    // Avaliação Fail-Fast: retorna false imediatamente se uma regra falhar
    return RuleEngine.<Transaction>create()
            .addRule(t -> t.getAmount() > 0)
            .addRule(t -> t.getStatus() == Status.PENDING)
            .addRule(SecurityPolicy::isSafeRegion)
            .evaluate(tx);
}

```

### Exemplo 3: Roteamento Sem `Switch-Case` com `Strategy`

**O Cenário:** Processar diferentes tipos de eventos de pagamento chamando serviços específicos.

```java
public ProcessResult handlePayment(PaymentEvent event) {
    // Retorna Opcional com o resultado ou aciona um fallback garantido
    return Strategy.<PaymentEvent, ProcessResult>create()
            .add(PaymentEvent::isCreditCard, CreditCardService::process)
            .add(PaymentEvent::isPix, PixService::generateCode)
            .executeOrElse(event, DefaultService::rejectUnknownMethod);
}

```

### Exemplo 4: Injeção de Dependência Funcional com `Curry`

**O Cenário:** Você quer usar uma regra de desconto dentro de um `Pipeline`, mas a regra exige a taxa atual do banco de dados.

```java
public void processOrder(Order order) {
    // 1. Regra base que exige 2 parâmetros (Taxa e Pedido)
    BiTransformer<Taxa, Order, Order> applyDiscount = (taxa, ord) -> ord.discount(taxa.value());
    
    // 2. Busca do BD
    Taxa currentTax = repository.getTodayTax();
    
    // 3. Aplicação Parcial: Congela a taxa e retorna um Transformer simples (apenas Pedido)
    Transformer<Order, Order> discountStep = Curry.partial(applyDiscount, currentTax);
    
    // 4. Conecta no pipeline naturalmente
    Pipeline.<Order>create()
            .addStep(discountStep)
            .addStep(Order::finalize)
            .execute(order);
}

```

---

*Módulo desenhado com foco em legibilidade, performance e isolamento de domínio. Sem fricção, sem dependências externas.*