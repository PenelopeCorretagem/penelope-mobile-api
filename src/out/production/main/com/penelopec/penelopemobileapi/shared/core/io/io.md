# 📁 Módulo IO (commons-core)

O pacote `io` oferece operações diretas para arquivos, caminhos e recursos de classpath usando exclusivamente as APIs nativas do Java.

## 1. O Problema: I/O Repetitivo e Inseguro

Operações de arquivo repetem abertura de recursos, normalização de caminhos e conversão de `IOException`. Sem validação, um caminho fornecido por usuário pode escapar da pasta autorizada; arquivos grandes lidos inteiramente também pressionam o heap.

## 2. A Solução: Acesso Controlado aos Recursos

Os utilitários centralizam operações recorrentes e validam entradas antes do acesso físico. `FileHelper` usa `FileChannel.transferTo` para cópia zero-copy, enquanto `PathUtils` resolve caminhos relativos de maneira segura.

* **Segurança:** resolução relativa à pasta base reduz path traversal.
* **Desempenho:** transferência de arquivos grandes não copia bytes desnecessariamente para o heap.
* **Simplicidade:** recursos de classpath são consumidos como texto ou linhas.

---

## 3. Guia Rápido dos Componentes

| Componente | Responsabilidade |
|------------|------------------|
| 📄 `FileHelper` e `ProgressListener` | Executam cópias com acompanhamento opcional. |
| 🧭 `PathUtils` | Manipula e valida caminhos do sistema de arquivos. |
| 📦 `ResourceLoader` | Carrega recursos disponíveis no classpath. |

Os utilitários centralizam detalhes de I/O em contratos coesos, null-safe e livres de dependências de runtime.

### Como escolher

Use `fastCopy` para arquivos, especialmente grandes; a sobrecarga com listener serve para acompanhar processos longos. Use `resolveSafe` para qualquer caminho relativo controlado por entrada externa. `ResourceLoader` é adequado apenas para conteúdo presente no classpath.

---

## 4. Exemplos de Uso Profissional

```java
Path upload = PathUtils.resolveSafe(uploadDirectory, requestedFileName);
FileHelper.fastCopy(temporaryFile, upload);

FileHelper.fastCopy(source, target,
	(processed, total) -> progressReporter.update(processed, total),
	10 * 1024 * 1024);

String template = ResourceLoader.readAsString("templates/notification.txt");
```

### Exemplo 2: Importação de arquivo com progresso observável

```java
ProgressListener progress = (processed, total) -> metrics.record(processed, total);
Path destination = PathUtils.resolveSafe(uploadDirectory, requestedFileName);
PathUtils.ensureDirectory(destination.getParent());
FileHelper.fastCopy(temporaryFile, destination, progress, 1_048_576L);
```

---

*Módulo desenhado para manter I/O eficiente, validado e isolado da regra de negócio.*