# Penelope Mobile API

Backend Java 21 da aplicação Penelope Mobile. Uma única aplicação Spring Boot fornece
autenticação, anúncios, favoritos, notificações, perfil e interpretação de pesquisa por IA.

## Funcionalidades

- Autenticação JWT, validação de sessão, recuperação e redefinição de senha.
- Consulta do catálogo de anúncios e detalhes de um imóvel.
- Gestão de perfil e favoritos autenticados.
- Caixa de entrada de notificações do usuário.
- Interpretação opcional de pesquisas imobiliárias com Gemini.
- Actuator e documentação OpenAPI.
- Perfil `dev` configurado para banco H2 em memória; as migrações Flyway definem o schema.

Rotas de negócio usam o contexto `/api`, por exemplo:

- `POST /api/v1/auth/login` e `POST /api/v1/auth/validate-access-token`
- `GET /api/v1/advertisements` e `GET /api/v1/advertisements/{id}`
- `GET /api/v1/users/me` e `PATCH /api/v1/users/me`
- `GET /api/v1/users/me/favorites`, `PUT` e `DELETE /api/v1/users/me/favorites/{advertisementId}`
- `GET /api/v1/users/me/notifications`
- `GET /api/actuator/health`
- `GET /api/swagger-ui/index.html`

### Pesquisa de imóveis com IA

`POST /api/v1/property-search/interpret` recebe a transcrição da fala e os catálogos de
cidade, região e tipo de anúncio enviados pelo app, e retorna filtros estruturados. A rota
é pública porque não acessa dados pessoais nem anúncios; a resposta segue o envelope de
erros padrão da API.

A interpretação não consulta nem retorna imóveis. Cidade, região e tipo ficam limitados aos
valores do catálogo informado. Dormitórios significam mínimo, preço significa máximo em
reais, bairro não é convertido em região e casa/apartamento permanece termo de pesquisa.
O app reconhece a fala no dispositivo/navegador e envia apenas a transcrição textual; os
filtros retornados podem ser revisados e são aplicados localmente aos anúncios carregados
pela API.

A chamada ao Gemini usa `store: false`. A chave permanece exclusivamente no backend, nunca
no app ou no bundle público.

## Requisitos

- JDK 21.
- Maven 3.9 ou superior.
- Chave Gemini para habilitar interpretação real; o restante da API funciona sem ela.

## Configuração local

Na raiz deste projeto, crie o `.env` local baseado no exemplo:

```powershell
Copy-Item .env.example .env
```

Configure os valores necessários:

- `GEMINI_API_KEY`: chave Gemini; mantenha-a local e não a compartilhe.
- `GEMINI_MODEL`: modelo de interpretação; o padrão é `gemini-3.6-flash`.
- `SERVER_PORT`: porta HTTP da API unificada; padrão `8080`.
- `JWT_API_KEY`: segredo para assinatura JWT. Configure um valor aleatório forte antes de
  usar os fluxos autenticados.
- `CORS_ALLOWED_ORIGINS`: origens Web permitidas pela API.
- `APP_FRONTEND_URL`: endereço usado nos links de redefinição de senha.
- `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME` e
  `SPRING_MAIL_PASSWORD`: configuração SMTP para envio de e-mails de recuperação.

O [application.yml](src/main/resources/application.yml) importa `.env` como configuração
local e mapeia `GEMINI_API_KEY` e `GEMINI_MODEL` para as propriedades Spring
`app.ai-search.gemini.api-key` e `app.ai-search.gemini.model`. Variáveis de ambiente do
processo podem sobrescrever os valores do arquivo. O `.env` real é ignorado pelo Git;
compartilhe apenas `.env.example`. O perfil `dev`, ativo por padrão, usa H2 em memória.

O app mobile tem seu próprio `.env`: o Expo não lê o arquivo do backend. Configure
`EXPO_PUBLIC_API_BASE_URL` com o endereço desta API incluindo `/api`; a mesma base serve
para o endpoint de pesquisa com IA. Para Android Emulator, use `10.0.2.2` no lugar de
`localhost`; em dispositivo físico, use o IP da máquina na rede local.

## Build e execução

Execute os comandos Maven na raiz deste projeto.

### Compilar e testar

```powershell
mvn clean verify
```

Compila a aplicação em Java 21 e executa os testes automatizados, incluindo os testes da
interpretação de pesquisa com Gemini simulado.

### Empacotar

```powershell
mvn package
```

O build produz um único artefato executável:

```text
target/penelope-mobile-api-1.0-SNAPSHOT.jar
```

### Iniciar a API

```powershell
java -jar target\penelope-mobile-api-1.0-SNAPSHOT.jar
```

A API usa a porta configurada em `SERVER_PORT` (padrão `8080`) e o contexto `/api`. Não há
um segundo servidor ou JAR para a pesquisa por IA. Sem `GEMINI_API_KEY`, a API inicia
normalmente e somente a interpretação responde HTTP 503.

### Verificar e chamar os endpoints

```powershell
Invoke-RestMethod http://localhost:8080/api/actuator/health
```

Envie um `POST` JSON para `http://localhost:8080/api/v1/property-search/interpret`:

```powershell
$body = @{
  transcript = "Apartamento com pelo menos dois quartos até 500 mil"
  cities = @("São Paulo")
  regions = @("Centro")
  propertyTypes = @("TODOS", "LANCAMENTO", "DISPONIVEL", "EM_OBRAS")
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/property-search/interpret" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

## Estrutura

- `src/main/java/.../auth`: autenticação e perfil.
- `src/main/java/.../catalog`: catálogo de anúncios.
- `src/main/java/.../favorite`: favoritos.
- `src/main/java/.../notification`: notificações.
- `src/main/java/.../search`: endpoint Spring de interpretação e integração Gemini.
- `src/main/java/.../shared`: componentes compartilhados da API.
- `src/main/resources/db/migration`: migrações Flyway.
- `src/test`: testes automatizados.
