# Roadmap técnico sugerido

Este documento reúne evoluções identificadas na auditoria do framework. Não é
uma promessa de release; cada item deve virar issue, design curto e testes antes
da implementação.

## Entregue neste ciclo

- Baseline Java 21 com Maven Enforcer para Java 21+/Maven 3.9+ e CI unificada.
- Plugins Maven antes implícitos agora possuem versões fixas; SBOM agregado
  CycloneDX é gerado em JSON e XML.
- Headers REST conservadores por padrão e ocultação configurável da versão.
- Limites globais de paginação, filtros, valores e ordenação no CRUD.
- Perfis JWT `compat`, `recommended` e `strict`, com validação de `typ`, `iat`,
  `jti`, `iss`, `aud`, idade máxima e `sub` no perfil estrito.

Os itens abaixo permanecem propostos ou dependem de infraestrutura externa.

## Prioridade alta

### Vincular sessões MCP à identidade autenticada

O `GET /mcp/sse` valida JWT quando a segurança está ativa, mas o `POST
/mcp/messages` ainda precisa revalidar a credencial e conferir que ela pertence
à sessão. Sugestão: armazenar na sessão um identificador não reversível do token
ou principal, exigir `Authorization` no POST, expirar sessões e não registrar o
`sessionId` em nível informativo.

**Aceite:** POST sem token, com token inválido ou de outro principal retorna 401;
sessão expirada retorna 404/410; testes cobrem sequestro de `sessionId`.

### Rate limit e brute force distribuídos e atômicos

A janela atual usa estruturas concorrentes, mas a sequência verificar/adicionar
não é atômica. Além disso, estado local não protege um cluster. Introduzir uma
abstração de storage com implementação local atômica e adapters opcionais para
Redis/JCache. A chave deve preferir usuário autenticado; headers de proxy só
podem ser aceitos quando proxies confiáveis estiverem configurados.

**Aceite:** testes concorrentes nunca excedem o limite; comportamento é
consistente entre dois nós; IP encaminhado não é confiado por padrão.

### Decidir o destino de `demoiselle-security-hashcash`

O diretório existe, mas não participa do reactor Maven nem do BOM e, portanto,
não é compilado pelo CI principal. Antes de incluí-lo, revisar criptografia,
configuração de segredo, replay, compatibilidade Jakarta EE e cobertura. Se a
funcionalidade não for mantida, marcar como descontinuada e removê-la em release
major.

**Aceite:** decisão arquitetural registrada; se mantido, módulo no reactor/BOM,
sem segredo padrão em `src/main/resources`, com testes e documentação; se
removido, nota de migração.

## Prioridade média

### Provedor de chaves JWT para rotação real

`activeKeyId` identifica a chave fallback, mas propriedades aninhadas de múltiplas
chaves não são carregadas atualmente. Criar SPI (`JwtKeyProvider`) para buscar
chaves por `kid` em arquivo, JWKS ou cofre, com cache e rotação controlados.

**Aceite:** `kid` desconhecido permanece rejeitado; chave antiga valida durante
a janela de rotação; falha do provedor é fail-closed; exemplos JWKS e testes de
rotação presentes.

### Backend de cache e chaves estruturadas

O cache CRUD é local e expira de forma preguiçosa. Criar SPI de cache, métricas
de hit/miss/eviction e chave estruturada que evite depender apenas de hash de
parâmetros. Preservar o namespace por entidade para invalidação.

**Aceite:** colisões de parâmetros não retornam dados incorretos; implementação
local tem limite de tamanho; adapter distribuído invalida em todos os nós.

### Concorrência no módulo Script

Uniformizar o locking de `eval`, `removeScript`, `unloadEngine`, `clearCache` e
operações de leitura. Hoje há sequências check-then-act que podem observar uma
engine removida por outra thread.

**Aceite:** stress tests concorrentes não produzem NPE nem estado corrompido;
política de ciclo de vida da engine documentada.

### Evoluir o contrato `Result<T>`

`PageResult` é imutável, mas implementa `setContent` lançando
`UnsupportedOperationException`, enquanto `ResultSet` é mutável. Em uma versão
major, separar leitura de mutação (`Result` e `MutableResult`) ou tornar todas as
implementações imutáveis.

**Aceite:** código genérico não falha conforme a implementação concreta; guia de
migração e compatibilidade binária avaliados.

### Gestão externa de segredos

Criar SPI para chaves e credenciais com adapters opcionais para Vault, AWS
Secrets Manager/KMS e Kubernetes Secrets. Propriedades sensíveis devem ser
redigidas em logs, e falhas do provedor não podem recuar para segredos default.

**Aceite:** rotação sem reinício quando suportada; cache com expiração; nenhum
segredo aparece em logs ou endpoints administrativos; testes de indisponibilidade
confirmam comportamento fail-closed.

### Idempotência e eventos transacionais

Adicionar suporte a `Idempotency-Key` para operações mutáveis e uma SPI de
Transactional Outbox para publicação após commit, com deduplicação e adapters
opcionais para Kafka, AMQP ou JMS.

**Aceite:** repetição da mesma chave não duplica efeitos; concorrência é atômica;
eventos não são publicados antes do commit; política de retenção documentada.

### Paginação por cursor e ciclo de vida de APIs

Oferecer keyset/cursor pagination com cursor assinado para grandes volumes e
headers padronizados de depreciação (`Deprecation`/`Sunset`). O CI deve detectar
breaking changes no contrato OpenAPI.

**Aceite:** cursor adulterado é rejeitado; paginação não perde/duplica registros
em inserções concorrentes cobertas por teste; mudanças incompatíveis bloqueiam o
pipeline ou exigem aprovação explícita.

## Qualidade e manutenção

- Definir um gate JaCoCo inicial realista e elevá-lo gradualmente; o default
  atual `0.00` apenas produz relatórios.
- Evoluir a supply chain com assinatura dos artefatos/SBOM, proveniência de
  build e verificação de reprodutibilidade; versões de plugins e geração do SBOM
  básico já fazem parte do reactor.
- Gerar inventário de módulos e contagens de testes no CI, evitando números
  fixos na documentação.
- Adicionar testes de contrato HTTP para headers malformados, RFC 9457, CORS,
  autenticação MCP e paginação.
- Publicar uma matriz de runtimes testados (WildFly, Open Liberty e Quarkus),
  distinguindo compatibilidade declarada de compatibilidade verificada.
