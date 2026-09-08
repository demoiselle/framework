---
layout: default
title: Roadmap técnico
description: Entregas do ciclo de modernização e extensões dependentes do ambiente dos produtos.
permalink: /docs/roadmap.html
---

# Roadmap técnico

Este documento registra o resultado da auditoria e da implementação do ciclo de
modernização do Demoiselle Framework. O baseline permanece **Java 21**, Maven
3.9+ e Jakarta EE 10. O inventário gerado do reactor está em
[`docs/generated/module-inventory.md`](generated/module-inventory.md).

## Entregue

### Segurança, rate limit e brute force

- `SecurityStore` define armazenamento atômico, namespaced e com TTL.
- `LocalSecurityStore` usa operações atômicas, limite global e eviction para
  resistir a cardinalidade não limitada.
- Rate limit e brute force compartilham esse contrato e possuem testes de
  concorrência.
- A chave prefere o principal autenticado e usa IP apenas como fallback.
  `X-Forwarded-For` é ignorado por padrão e só é aceito quando o peer imediato
  consta em `demoiselle.security.trustedProxies`.
- `demoiselle.security.storeMaxEntries` limita o backend local.

### MCP autenticado e limitado

- Sessões SSE são vinculadas a fingerprint não reversível da identidade.
- `POST /mcp/messages` revalida `Authorization` e rejeita token ausente,
  inválido ou pertencente a outro principal.
- O fallback sem integração JWT é fail-closed.
- TTL absoluto, idle timeout, teto de sessões e rate limit de tools são
  configuráveis. Sessões desconhecidas, expiradas e excesso de capacidade
  possuem respostas HTTP distintas.
- Identificadores de sessão não são registrados em nível informativo.

### HashCash

- `demoiselle-security-hashcash` participa do reactor e do BOM.
- Desafios são assinados com HS256, vinculados a recurso, `jti`, expiração e
  dificuldade; o trabalho usa SHA-256.
- O segredo não possui default e deve ter pelo menos 32 bytes.
- Replay é bloqueado atomicamente pelo `SecurityStore`.

### SPIs de chaves, cache e segredos

- `JwtKeyProvider` suporta múltiplos `kid`, refresh e janela de rotação.
  `LocalJwtKeyProvider` lê material DER/base64; chave vencida pode verificar
  durante a janela, mas nunca assinar. `kid` desconhecido continua fail-closed.
- `CacheBackend` usa `CacheKey` estruturada. O backend local é LRU, limitado,
  possui TTL, hit/miss/eviction e invalidação por namespace.
- `SecretProvider` e `SecretResolver` fornecem resolução fail-closed via
  ServiceLoader. Providers incluídos: ambiente (`env`), system property (`sys`)
  e arquivo (`file`), com cache TTL e sem logar valores.
- Adapters para JWKS, Redis/JCache, Vault, AWS e Kubernetes podem implementar os
  SPIs sem tornar SDKs ou serviços externos dependências obrigatórias do reactor.

### Concorrência e contratos

- O cache global de Script é `@ApplicationScoped` e usa `ReadWriteLock` por
  engine. Criação, load, update, remove, unload e clear são serializados; eval e
  leituras usam read lock. Há stress test concorrente.
- `Result<T>` é contrato de leitura. `MutableResult<T>` concentra mutação;
  `PageResult` permanece imutável e `ResultSet` é mutável. A ponte
  `Result#setContent` permanece deprecated apenas para transição.

### Dados e ciclo de vida HTTP

- `@Idempotent`, `IdempotencyStore` e filtro REST implementam aquisição atômica,
  conflito para requisição em andamento, replay de respostas 2xx e liberação em
  erro. Payloads de resposta são preservados como bytes/String ou JSON Jackson,
  nunca por `Object#toString()`.
- Transactional Outbox inclui store/publisher SPIs, deduplicação, retenção e
  publicação no observer `AFTER_SUCCESS`. O store local só recebe a mensagem
  depois do commit; rollback não deixa registro publicável.
- Cursor/keyset pagination usa cursor HMAC-SHA256 base64url, expiração,
  comparação constant-time, direção BEFORE/AFTER, ordenação ASC/DESC e
  tie-breaker final. O suporte REST adiciona `Link rel=next` sem remover `range`.
- `@ApiLifecycle` emite `Deprecation`, `Sunset` e links de documentação/sucessor
  sem sobrescrever headers definidos pela aplicação.

### Qualidade, CI e supply chain

- JaCoCo 0.8.15 possui gates iniciais de 5% de instruções e 1% de branches.
- CycloneDX gera SBOM agregado schema 1.6 em JSON/XML.
- Builds usam `project.build.outputTimestamp`, versões explícitas de plugins,
  GPG com best practices e checksums/proveniência.
- Actions estão pinadas por SHA; jobs possuem timeout e concurrency cancellation.
- Inventário de módulos/testes e matriz de runtimes são gerados dinamicamente.
  O inventário usa `--check` e falha para módulo ausente, sem POM ou fora do
  reactor.
- O gate OpenAPI detecta remoção de operações e novos parâmetros obrigatórios
  quando uma spec versionada/gerada existe no projeto consumidor.
- Scripts autocontidos possuem smoke tests.

## Extensões dependentes do ambiente

Os itens abaixo não são código obrigatório do framework porque exigem produto,
credenciais, infraestrutura ou runtime escolhidos pela aplicação. Os SPIs e
gates necessários já estão disponíveis; cabe a cada produto selecionar e operar
os adapters compatíveis com sua arquitetura.

| Extensão | Integração do produto | Benefício principal |
|---|---|---|
| Estado distribuído | `SecurityStore`, `CacheBackend` e `IdempotencyStore` sobre Redis/JCache ou equivalente | Mantém segurança, cache e idempotência consistentes entre réplicas |
| Chaves e segredos gerenciados | `JwtKeyProvider` e `SecretProvider` para JWKS, Vault, AWS ou Kubernetes | Centraliza rotação, auditoria e controle de acesso a material sensível |
| Outbox persistente e broker | `OutboxStore` transacional e `OutboxPublisher` para JPA, Kafka, AMQP ou JMS | Evita perda de eventos na janela entre commit e publicação |
| Matriz executada de runtimes | Jobs com imagens versionadas de WildFly, Open Liberty, Quarkus ou runtime adotado | Produz evidência contínua de portabilidade e compatibilidade |
| Baseline OpenAPI da aplicação | Spec versionada ou gerada e comparada em cada mudança | Bloqueia quebras de contrato antes que atinjam consumidores |
| Reprodutibilidade por plataforma | Builds herméticos e comparação bloqueante de artefatos | Reforça auditoria e confiança na supply chain da release |

O guia **[Extensões de produção para produtos Demoiselle](production-extensions.md)**
explica, para cada ponto, o problema resolvido, os benefícios para produtos
construídos com o framework, critérios do adapter, estratégia de adoção e testes
necessários.

Todas as extensões devem preservar os invariantes fail-closed, atomicidade e
limites do framework, não introduzir segredos padrão e incluir testes de
indisponibilidade, concorrência, retry, rollback e rotação.
