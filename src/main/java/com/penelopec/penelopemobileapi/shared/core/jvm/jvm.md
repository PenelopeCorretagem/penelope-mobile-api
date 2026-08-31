# 🖥️ Módulo JVM (commons-core)

O pacote `jvm` concentra diagnósticos de runtime para observar aspectos essenciais da máquina virtual durante desenvolvimento, testes e suporte.

## 1. O Problema: A JVM Como Caixa-Preta

Sem métricas de runtime, consumo de memória e lentidão são percebidos tarde e investigados por suposição. Consultas a MXBeans espalhadas pela aplicação ainda produzem diagnósticos inconsistentes.

## 2. A Solução: Diagnóstico em APIs Pequenas

Cada classe expõe um aspecto da máquina virtual, deixando para a aplicação a decisão de coletar, registrar ou alertar. Assim, a infraestrutura de gerenciamento da JDK permanece centralizada.

* **Memória:** uso global, old generation e thread dump.
* **GC:** contagem, duração de pausas e métricas por algoritmo.
* **Execução:** uptime, JIT e carregamento de classes.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 🧩 `ClassLoadingUtils` | Consulta informações sobre carregamento de classes. |
| ♻️ `GCMetrics` | Obtém métricas relacionadas aos coletores de lixo. |
| 🧠 `HeapMonitor` | Monitora o uso de heap da JVM. |
| 🔬 `JvmDiagnostics` | Reúne diagnósticos gerais do processo Java. |

O módulo é voltado a observabilidade e diagnóstico; cada aplicação define como expor ou armazenar as informações retornadas.

### Como escolher

`HeapMonitor` atende sinais de pressão de memória e investigações de threads. `GCMetrics` ajuda a correlacionar latência e garbage collection. `ClassLoadingUtils` é útil para integrações opcionais; `JvmDiagnostics` compõe telemetria de processo.

---

## 4. Exemplos de Uso Profissional

```java
if (HeapMonitor.isGlobalHeapCritical(85.0)) {
	alertService.notify("Heap acima do limite operacional");
}

boolean critical = GCMetrics.isGCPauseTimeCritical(2_000);
long pauses = GCMetrics.getTotalGCPauseTimeMillis();

if (ClassLoadingUtils.isPresent("com.example.OptionalIntegration")) {
	integration.enable();
}
```

### Exemplo 2: Telemetria de uptime e compilação JIT

```java
long uptime = JvmDiagnostics.getUptimeMillis();
String compiler = JvmDiagnostics.getJitCompilerName();
JvmDiagnostics.getJitCompilationTimePercentage()
	.ifPresent(percentage -> metrics.jit(compiler, uptime, percentage));
```

---

*Módulo desenhado para observabilidade leve da JVM, sem agentes ou dependências externas.*