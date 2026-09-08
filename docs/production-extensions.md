---
layout: default
title: Extensões de produção
description: Como adaptar estado, segredos, mensageria, runtimes e supply chain às necessidades de produtos Demoiselle.
permalink: /docs/production-extensions.html
---

# Extensões de produção para produtos Demoiselle

O Demoiselle fornece implementações locais e contratos estáveis para que uma
aplicação possa ser desenvolvida, testada e executada sem Redis, Vault, brokers
ou runtimes externos obrigatórios. Em produção, cada produto pode substituir
esses componentes por adapters alinhados à sua topologia, infraestrutura e
políticas corporativas.

As extensões deste guia não são pendências funcionais do framework. Elas são
pontos de integração que pertencem ao produto porque dependem de decisões como
provedor de nuvem, banco de dados, broker, runtime Jakarta EE, requisitos de
alta disponibilidade e processo de release.

## Visão geral

| Extensão | Quando se torna importante | Principal benefício para o produto |
|---|---|---|
| Estado distribuído | Duas ou mais instâncias atendem a mesma API | Comportamento consistente de segurança, cache e idempotência em todo o cluster |
| Chaves e segredos gerenciados | Há rotação, segregação de ambientes ou política central de credenciais | Redução da exposição de material sensível e rotação sem alterar o código de negócio |
| Outbox persistente e broker | Uma transação de negócio precisa produzir eventos confiáveis | Eliminação da janela de dual write entre banco e mensageria |
| Matriz executada de runtimes | O produto suporta ou migra entre diferentes runtimes | Evidência contínua de portabilidade e detecção precoce de incompatibilidades |
| Baseline OpenAPI da aplicação | A API possui consumidores independentes | Governança de contrato e bloqueio de mudanças incompatíveis antes do deploy |
| Reprodutibilidade por plataforma | O artefato precisa ser auditável e reconstruível | Maior confiança na supply chain e equivalência entre artefato testado e publicado |

Não é necessário adotar os seis itens de uma vez. Um serviço interno com uma
única instância pode usar os backends locais, enquanto uma API pública escalada
horizontalmente normalmente prioriza estado distribuído, segredos gerenciados,
outbox e governança OpenAPI.

## 1. Estado distribuído

### Problema resolvido

Os backends locais mantêm dados somente na JVM. Em um cluster, duas requisições
do mesmo usuário podem chegar a instâncias diferentes. Sem estado compartilhado,
cada instância enxerga contadores, respostas idempotentes e entradas de cache
distintos.

Isso pode permitir que um cliente multiplique o limite de requisições pelo
número de réplicas, contorne bloqueios de força bruta, repita uma operação já
concluída ou receba dados de cache incoerentes.

### Contratos disponíveis

- [`SecurityStore`](https://github.com/demoiselle/framework/blob/master/demoiselle-security/src/main/java/org/demoiselle/jee/security/store/SecurityStore.java):
  contadores e locks atômicos, namespaces, TTL, janela de rate limit e marcadores
  de uso único para proteção contra replay.
- [`CacheBackend`](https://github.com/demoiselle/framework/blob/master/demoiselle-crud/src/main/java/org/demoiselle/jee/crud/cache/CacheBackend.java):
  cache por `CacheKey` estruturada, TTL, métricas e invalidação por namespace.
- [`IdempotencyStore`](https://github.com/demoiselle/framework/blob/master/demoiselle-core/src/main/java/org/demoiselle/jee/core/api/idempotency/IdempotencyStore.java):
  aquisição atômica de uma operação, conclusão com resposta preservada, aborto e
  consulta durante o TTL.

Adapters podem usar Redis, JCache, Infinispan, Hazelcast ou outro armazenamento
compartilhado, desde que preservem integralmente a semântica do SPI.

### Como auxilia o produto

- Aplica rate limit e bloqueio de força bruta ao cliente, e não à réplica que o
  atendeu.
- Impede replay de desafios e duplicação de comandos mesmo com balanceamento de
  carga ou retry em outra instância.
- Permite que uma resposta idempotente seja recuperada por qualquer réplica.
- Mantém invalidação de cache consistente após alterações de dados.
- Facilita scale-out e rolling update sem depender de afinidade de sessão.

### Requisitos do adapter

1. As operações de incremento, expiração e `markIfAbsent` devem ser atômicas;
   um fluxo `GET` seguido de `SET` não satisfaz o contrato.
2. TTL deve ser aplicado no backend, inclusive para registros abandonados após
   falha de processo.
3. Namespaces e a representação completa de `CacheKey` devem fazer parte da
   chave física para evitar colisões entre produtos, tenants ou entidades.
4. A conclusão idempotente deve armazenar status HTTP, headers e bytes da
   resposta sem conversão por `toString()`.
5. Quotas, eviction, latência, erros e uso de conexões precisam de métricas e
   alertas.
6. Uma indisponibilidade não deve provocar fallback silencioso para memória
   local: isso divide o estado e enfraquece as garantias. O produto deve definir
   explicitamente se rejeita a operação, degrada apenas cache de leitura ou usa
   outra estratégia segura.

### Quando adotar

Adote antes de habilitar mais de uma réplica para endpoints protegidos por rate
limit, idempotência ou anti-replay. Para cache puramente oportunista, o backend
local ainda pode ser suficiente se inconsistência temporária for aceitável e
estiver documentada.

## 2. Chaves e segredos gerenciados

### Problema resolvido

Chaves JWT, senhas e tokens armazenados em arquivos de configuração aumentam o
risco de vazamento, dificultam rotação e normalmente exigem rebuild ou restart
para cada alteração. Ambientes regulados também precisam de trilha de auditoria,
controle de acesso e separação entre quem desenvolve e quem administra segredos.

### Contratos disponíveis

- [`JwtKeyProvider`](https://github.com/demoiselle/framework/blob/master/demoiselle-security-jwt/src/main/java/org/demoiselle/jee/security/jwt/api/JwtKeyProvider.java):
  seleciona a chave ativa de assinatura e resolve chaves de verificação por
  `kid`, com suporte a rotação.
- [`SecretProvider`](https://github.com/demoiselle/framework/blob/master/demoiselle-configuration/src/main/java/org/demoiselle/jee/configuration/secret/SecretProvider.java):
  resolve referências opacas a segredos. O framework inclui os schemes `env`,
  `sys` e `file`; adapters podem adicionar Vault, AWS Secrets Manager,
  Kubernetes Secrets ou serviços equivalentes.

Um `JwtKeyProvider` pode consumir JWKS para verificação ou integrar o mecanismo
de chaves adotado pela organização. Um `SecretProvider` deve ser registrado pelo
mecanismo de extensão documentado pelo SPI e manter o valor resolvido fora de
logs e mensagens de erro.

### Como auxilia o produto

- Centraliza rotação e revogação sem espalhar material criptográfico em cada
  repositório.
- Permite políticas diferentes para desenvolvimento, homologação e produção
  sem alterar o código de negócio.
- Reduz o tempo de exposição de uma chave comprometida.
- Viabiliza auditoria de acesso e princípio do menor privilégio no provedor.
- Facilita blue/green e rolling deployment: chaves antiga e nova podem coexistir
  durante a janela de rotação.

### Requisitos do adapter

1. `kid` desconhecido ou fora da janela de rotação deve retornar ausência e
   causar rejeição do token; nunca selecione outra chave por conveniência.
2. A chave vencida pode verificar durante a janela configurada, mas não deve
   continuar assinando.
3. Cache de JWKS ou segredos precisa de TTL finito, refresh controlado e proteção
   contra thundering herd.
4. Falha de rede, permissão ou entrada ausente deve ser fail-closed. Não use
   segredo default e não aceite valor vazio silenciosamente.
5. Valores, payloads de erro e credenciais de autenticação do provider não podem
   aparecer em logs, traces ou métricas.
6. A identidade da aplicação deve ter acesso apenas às chaves necessárias ao
   seu ambiente e finalidade; deployments que apenas validam tokens não
   precisam receber chave privada.

### Quando adotar

Use providers gerenciados sempre que houver rotação periódica, múltiplos
ambientes, requisitos de auditoria ou mais de uma equipe operando o produto.
`env`, `sys` e `file` continuam úteis para desenvolvimento, testes e plataformas
que já injetam o segredo de forma segura nesses canais.

## 3. Outbox persistente e broker

### Problema resolvido

Gravar uma alteração no banco e publicar uma mensagem são duas operações
independentes. Se o processo falhar entre elas, o banco pode confirmar a
transação sem emitir o evento, ou o evento pode ser emitido para uma transação
que posteriormente sofre rollback. Esse problema é conhecido como dual write.

### Contratos disponíveis

- [`OutboxStore`](https://github.com/demoiselle/framework/blob/master/demoiselle-core/src/main/java/org/demoiselle/jee/core/api/outbox/OutboxStore.java):
  persiste mensagens, suprime duplicatas, lista pendências, registra sucesso ou
  falha e aplica retenção.
- [`OutboxPublisher`](https://github.com/demoiselle/framework/blob/master/demoiselle-core/src/main/java/org/demoiselle/jee/core/api/outbox/OutboxPublisher.java):
  entrega uma `OutboxMessage` ao broker escolhido.
- [`OutboxService`](https://github.com/demoiselle/framework/blob/master/demoiselle-core/src/main/java/org/demoiselle/jee/core/outbox/OutboxService.java):
  coordena staging, publicação em `AFTER_SUCCESS` e retry por lotes.

Um store de banco realmente transacional deve inserir a mensagem na mesma
transação da alteração de negócio e retornar `true` em
`participatesInTransaction()`. Em rollback, a linha da outbox também desaparece.
Somente depois do commit o publisher é acionado. Um scheduler pode chamar
`OutboxService.flush(int)` para reenviar registros `PENDING` ou `FAILED`.

### Como auxilia o produto

- Fecha a janela entre commit no banco e envio ao broker.
- Permite retry após indisponibilidade de Kafka, AMQP, JMS ou webhook sem perder
  o evento de negócio.
- Preserva ordenação operacional por data de criação no lote de pendências.
- Oferece deduplicação no produtor e uma trilha consultável do estado da
  publicação.
- Desacopla a regra de negócio da tecnologia de mensageria.

### Requisitos do adapter

1. `append()` deve participar da mesma transação e conexão lógica usadas pelo
   caso de uso; retornar `participatesInTransaction() = true` sem esse vínculo
   quebra a garantia do padrão.
2. `dedupeKey` deve possuir restrição única no armazenamento para evitar corrida
   entre réplicas.
3. A consulta de pendências precisa suportar concorrência entre workers, por
   exemplo com claim/lock e timeout, evitando publicação simultânea do mesmo
   registro.
4. Exceções do publisher devem manter a mensagem elegível para retry, com
   métricas de tentativas, idade da pendência e dead-letter quando aplicável.
5. A retenção de mensagens publicadas deve ser executada periodicamente.
6. Entrega por brokers costuma ser pelo menos uma vez; consumidores também
   devem ser idempotentes. O `dedupeKey` pode compor a chave de deduplicação do
   consumidor.
7. Em produção, forneça um publisher real e valide sua presença no startup. O
   publisher noop existe para execução sem broker, não para confirmar entrega
   externa.

### Quando adotar

Use outbox persistente quando o sucesso de uma transação precisar produzir um
evento, integração ou comando externo confiável. Publicação direta ainda pode
ser adequada para notificações descartáveis que não fazem parte da consistência
do processo de negócio.

## 4. Matriz executada de runtimes

### Problema resolvido

Compatibilidade declarada com Jakarta EE 10 não prova que uma aplicação real
inicia e funciona da mesma forma em todos os runtimes. Diferenças de CDI, JPA,
JAX-RS, classloading, configuração, build e native image aparecem apenas quando
o produto é implantado e exercitado naquele ambiente.

O framework gera uma matriz que separa suporte declarado de suporte verificado.
A certificação efetiva precisa usar imagens, versões e configurações mantidas
pelo produto ou pelo processo de release.

### Como auxilia o produto

- Detecta incompatibilidades antes de uma atualização de runtime chegar à
  produção.
- Torna portabilidade uma evidência de CI, em vez de uma expectativa baseada
  apenas nas APIs usadas.
- Reduz risco de lock-in e custo de migração entre WildFly, Open Liberty,
  Quarkus ou outro runtime compatível.
- Registra exatamente quais combinações de Java, runtime e modo de empacotamento
  foram aprovadas para cada release.
- Antecipa diferenças entre JVM tradicional, container e native image.

### Como implementar a certificação

1. Crie uma aplicação de smoke representativa do produto, incluindo os módulos
   realmente usados: configuração, REST, segurança, JWT, CRUD/JPA, health e
   integrações externas relevantes.
2. Fixe a imagem do runtime por digest ou versão imutável e registre também JDK,
   driver JDBC e banco utilizado.
3. Para cada combinação, faça build, deploy, aguarde readiness e execute testes
   de contrato HTTP, autenticação, persistência e shutdown.
4. Colete logs, relatórios e versão efetiva do runtime como artefatos da CI.
5. Execute a matriz em pull requests para mudanças de infraestrutura e, no
   mínimo, antes de cada release.
6. Atualize a matriz gerada somente quando existir evidência de execução; não
   converta suporte declarado em verificado manualmente.

### Quando adotar

Uma única célula de matriz já agrega valor: o runtime oficial de produção. Expanda
para outros runtimes quando o produto assumir portabilidade como requisito ou
estiver preparando uma migração.

## 5. Baseline OpenAPI da aplicação

### Problema resolvido

O Demoiselle fornece infraestrutura e contributors OpenAPI, mas não conhece os
endpoints de negócio de cada produto. Sem uma especificação versionada ou gerada,
não existe contrato anterior contra o qual a CI possa identificar uma mudança
incompatível.

O gate incluído no framework detecta, quando há baseline e candidato, remoção de
operações e introdução de parâmetros obrigatórios. O produto pode complementar
essa verificação com regras adicionais de compatibilidade.

### Como auxilia o produto

- Impede que uma alteração incompatível seja descoberta somente pelos
  consumidores após o deploy.
- Permite geração confiável de clientes, mocks, testes de contrato e portais de
  API.
- Torna depreciação e remoção de endpoints uma decisão explícita e revisável.
- Aproxima documentação e implementação, reduzindo divergência entre equipes.
- Fornece histórico do contrato publicado por versão do produto.

### Como estabelecer o baseline

1. Gere a spec OpenAPI a partir da aplicação representativa ou mantenha uma spec
   canônica revisada junto ao código.
2. Normalize a saída para evitar diferenças de ordenação e valores voláteis.
3. Guarde como baseline a spec da última versão publicada, não apenas a versão
   existente na branch do pull request.
4. Na CI, gere o candidato e execute o gate contra o baseline.
5. Bloqueie mudanças incompatíveis ou exija uma exceção formal acompanhada de
   versionamento, período de depreciação e comunicação aos consumidores.
6. Publique a spec aprovada junto com o artefato e associe seu checksum à
   release.

### Quando adotar

Adote desde o primeiro consumidor externo ou desde o primeiro cliente gerado.
Para APIs internas, o baseline continua útil quando frontend, backend e
integrações são liberados em cadências diferentes.

## 6. Reprodutibilidade por plataforma

### Problema resolvido

Dois builds do mesmo commit podem produzir artefatos diferentes por causa da
versão do JDK ou Maven, timezone, locale, ordem de arquivos, timestamps,
dependências resolvidas, plugins ou imagem base. Uma verificação informativa
mostra diferenças, mas não impede a publicação de um artefato não reproduzível.

O reactor já fixa `project.build.outputTimestamp` e compara JARs normalizados.
Para transformar esse sinal em gate bloqueante, o produto precisa controlar o
ambiente completo do build.

### Como auxilia o produto

- Aumenta a confiança de que o binário publicado corresponde ao código revisado
  e testado.
- Permite reconstrução independente durante auditoria ou resposta a incidente.
- Detecta dependências mutáveis, geração não determinística e contaminação do
  ambiente de build.
- Reforça SBOM, checksums e proveniência com evidência de repetibilidade.
- Reduz diferenças entre o artefato produzido na CI e aquele promovido entre
  ambientes.

### Como tornar a checagem bloqueante

1. Use imagem hermética e imutável, preferencialmente fixada por digest, com JDK,
   Maven e ferramentas conhecidas.
2. Comece sempre de checkout e repositório Maven limpos; controle mirrors e
   impeça resolução de dependências mutáveis.
3. Fixe timezone, locale, encoding, usuário, permissões e variáveis que afetam o
   build.
4. Execute dois builds isolados do mesmo commit e compare artefatos, SBOMs e
   checksums. A normalização deve ignorar apenas metadados comprovadamente
   irrelevantes.
5. Investigue cada divergência antes de ampliar a lista de exclusões.
6. Após um período estável, remova `continue-on-error` do job e torne a diferença
   uma falha de release.
7. Armazene checksums, SBOM e proveniência junto à release para verificação
   posterior.

### Quando adotar

Comece de forma informativa para medir a estabilidade. Torne o gate bloqueante
para releases oficiais, especialmente em produtos regulados, distribuídos a
terceiros ou sujeitos a requisitos formais de supply chain.

## Princípios comuns aos seis adapters

Toda extensão de produção deve preservar os invariantes do framework:

- **Fail-closed:** indisponibilidade não pode liberar autenticação, aceitar `kid`
  desconhecido, ignorar idempotência ou substituir segredo por default.
- **Atomicidade:** operações definidas como atômicas pelo SPI não podem ser
  implementadas por sequências vulneráveis a corrida.
- **Limites:** caches, filas, retries, conexões e registros precisam de TTL,
  retenção, backpressure e cotas.
- **Observabilidade sem vazamento:** registre latência, status, tamanho e erros,
  nunca tokens, payloads sensíveis ou valores de segredos.
- **Isolamento:** inclua produto, ambiente e tenant nos namespaces quando
  diferentes workloads compartilham a infraestrutura.
- **Testes de falha:** cubra timeout, indisponibilidade, retry, concorrência,
  rotação, rollback e recuperação após restart.
- **Compatibilidade:** mantenha testes de contrato contra o SPI para permitir a
  evolução independente do framework e do adapter.

## Sequência recomendada de adoção

1. Mantenha as implementações locais em desenvolvimento e nos testes unitários
   rápidos.
2. Crie cada adapter em módulo separado do produto para isolar SDKs externos do
   domínio e do framework.
3. Execute a mesma suíte de contrato contra o backend local e o backend real.
4. Valide indisponibilidade e recuperação em ambiente de integração.
5. Faça rollout gradual, acompanhe métricas e preserve um procedimento explícito
   de rollback que não enfraqueça garantias de segurança ou consistência.
6. Registre no runbook do produto quais extensões estão ativas, seus owners,
   SLOs, limites e procedimentos de rotação ou recuperação.

Com essa separação, o Demoiselle mantém o núcleo leve e portável, enquanto cada
produto obtém as garantias de escala, segurança, consistência e governança
exigidas por seu ambiente de produção.
