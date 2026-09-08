# Migração para os contratos de segurança e dados da versão 4.1

Este guia complementa a migração Jakarta EE 10 existente em
[`docs/index.md`](index.md). As mudanças preservam compatibilidade onde seguro;
defaults de autenticação e segredos são fail-closed.

## `Result` e `MutableResult`

Use `Result<T>` quando o consumidor só precisa ler conteúdo:

```java
Result<Produto> result = service.find();
List<Produto> produtos = result.getContent();
```

Implementações que permitem alteração devem implementar `MutableResult<T>`:

```java
MutableResult<Produto> result = new ResultSet<>();
result.setContent(produtos);
```

`Result#setContent` permanece temporariamente como ponte deprecated. Não chame
esse método em `PageResult`, que é imutável.

## MCP seguro

Com `demoiselle.mcp.security.enabled=true`, inclua `demoiselle-security-jwt`. O
fallback sem validador real rejeita todas as credenciais. Tanto o GET SSE quanto
cada POST exigem `Authorization: Bearer ...`; o token do POST deve resolver para
a mesma identidade vinculada à sessão.

```properties
demoiselle.mcp.security.enabled=true
demoiselle.mcp.sessionTtlMillis=1800000
demoiselle.mcp.sessionIdleMillis=300000
demoiselle.mcp.maxSessions=10000
demoiselle.mcp.toolRateLimitRequests=60
demoiselle.mcp.toolRateLimitWindowSeconds=60
```

## HashCash

Adicione o módulo pelo BOM e configure um segredo exclusivo de pelo menos 32
bytes. Não existe segredo padrão:

```xml
<dependency>
  <groupId>org.demoiselle.jee</groupId>
  <artifactId>demoiselle-security-hashcash</artifactId>
</dependency>
```

```properties
demoiselle.security.hashcash.hashcashKey=${secret:env:HASHCASH_KEY}
demoiselle.security.hashcash.timetoLiveMilliseconds=300000
demoiselle.security.hashcash.difficultyBits=20
```

Desafios antigos sem recurso, `jti`, `bits` e assinatura válida não são aceitos.

## Chaves JWT rotativas

A configuração local compacta é uma lista separada por `;`:

```properties
demoiselle.security.jwt.activeKeyId=key-2026
demoiselle.security.jwt.keys=key-2025:PUBLIC_DER_B64:-:1798761600000;key-2026:PUBLIC_DER_B64:PRIVATE_DER_B64
demoiselle.security.jwt.keyRefreshSeconds=300
demoiselle.security.jwt.keyRotationWindowSeconds=3600
```

Cada entrada usa
`kid:publicKeyBase64Der[:privateKeyBase64Der[:validUntilEpochMillis]]`. Use `-`
para uma chave apenas de verificação. Um `kid` explícito desconhecido nunca cai
na chave fallback.

## Referências de segredo

Valores de configuração podem apontar para providers incluídos:

```properties
app.password=${secret:env:APP_PASSWORD}
app.token=${secret:sys:app.token}
app.privateKey=${secret:file:/run/secrets/private-key}
```

A propriedade de sistema
`demoiselle.configuration.secret.cache.ttl.seconds` controla o cache (300 por
padrão; `0` desabilita). Provider ausente ou indisponível causa erro; não há
fallback para texto default.

## Proxy e estado de segurança

Headers encaminhados são ignorados até que o peer imediato seja confiável:

```properties
demoiselle.security.trustedProxies=10.0.0.10,10.0.0.11
demoiselle.security.storeMaxEntries=100000
```

Não adicione ranges genéricos sem validar a topologia. Para cluster, forneça uma
implementação distribuída de `SecurityStore`.

## Idempotência, outbox e cursor

- Anote somente operações mutáveis elegíveis com `@Idempotent`; clientes devem
  enviar `Idempotency-Key` estável por intenção de negócio.
- Para garantia atômica com banco, forneça `OutboxStore` que retorne
  `participatesInTransaction() == true`; o store local é apropriado para testes e
  processo único.
- Cursor é aditivo: endpoints existentes podem continuar usando `range`. Sempre
  termine a ordenação keyset com campo único (normalmente a chave primária).

## Depreciação de APIs

Use `@ApiLifecycle` em recurso ou método. O filtro só adiciona `Deprecation`,
`Sunset` e `Link` quando a aplicação ainda não definiu esses headers, permitindo
uma migração gradual sem sobrescrever políticas existentes.
