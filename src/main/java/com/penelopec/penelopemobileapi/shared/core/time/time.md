# 🗓️ Módulo Time (commons-core)

O pacote `time` organiza operações recorrentes de datas, horários, intervalos e períodos sobre a API moderna `java.time`.

## 1. O Problema: Regras Temporais Ambíguas

Comparações de datas duplicadas e formatação inconsistente geram erros de fronteira e interfaces difíceis de manter.

## 2. A Solução: Tempo Como Valor de Domínio

Intervalos, interseções, conversões para BRT e formatos humanos ficam concentrados em operações imutáveis da JDK.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 📆 `DateRange` | Representa e consulta intervalos entre datas. |
| ⏳ `PeriodFormatter` | Formata períodos de maneira legível. |
| 🌎 `TimeUtils` | Reúne operações utilitárias sobre tipos temporais da JDK. |

Prefira os tipos imutáveis de `java.time` e use este módulo para expressar regras temporais com intenção visível.

`DateRange` responde por inclusão, sobreposição e interseção; `PeriodFormatter` apresenta durações; `TimeUtils` concentra conversão e formato brasileiro.

---

## 4. Exemplos de Uso Profissional

```java
DateRange period = DateRange.of(startDate, endDate);
boolean active = period.contains(LocalDate.now());
String elapsed = PeriodFormatter.formatStopwatch(duration);
```

### Exemplo 2: Comunicação de prazo no horário brasileiro

```java
LocalDate dueDate = TimeUtils.parseBr("30/08/2026");
String displayDate = TimeUtils.formatBr(dueDate);
ZonedDateTime localEvent = TimeUtils.toBrt(event.createdAt());
String auditTime = TimeUtils.formatIsoWithOffset(localEvent.toInstant(), TimeUtils.BRT_ZONE);
notification.send("Vencimento em " + displayDate, auditTime);
```

---

*Módulo desenhado para que regras temporais sejam legíveis e livres de mutação.*