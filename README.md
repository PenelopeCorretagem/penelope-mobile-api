# Penelope Mobile API

Projeto Java 21 iniciado para a API do Penelope Mobile.

![Java 21+](https://img.shields.io/badge/Java-21%2B-ed8b00?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/build-Maven-c71a36?logo=apachemaven&logoColor=white)
![Runtime](https://img.shields.io/badge/runtime-JDK%20only-2e7d32)

Este repositório contém a base inicial da API do Penelope Mobile. A aplicação e suas regras de negócio ainda estão em construção.

## Shared Core

O pacote `com.penelopec.penelopemobileapi.shared.core` representa o espaço para componentes técnicos reutilizáveis entre os futuros módulos da API. Ele será evoluído conforme as necessidades reais do sistema forem definidas.

## Requisitos

- Java 21 ou superior.
- Maven 3.9 ou superior.

## Build

```bash
mvn verify
```

## Insígnia de educação

O detalhe `GET /api/v1/advertisements/{id}` pode incluir
`estate.educationBadge`, calculado pelo projeto `penelope-data-intelligence`
para `empreendimento.id`. A API consulta o banco analítico em modo somente
leitura; a listagem de anúncios não consulta insígnias.

Configure `BADGES_ENABLED=true`, `BADGES_DB_URL`, `BADGES_DB_USER` e
`BADGES_DB_PASSWORD`. No MySQL local do projeto Python, a URL para a API
executada no host é `jdbc:mysql://localhost:3307/penelope_etl`. Com a
integração desligada, o campo é omitido. A documentação completa da carga,
regra, estados e validação está em
`../penelope-data-intelligence/docs/fluxo-completo-insignia-educacao.md`.
