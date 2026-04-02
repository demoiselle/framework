---
layout: default
title: Demoiselle Framework v4 — Modernização Jakarta EE 10
---

# Demoiselle Framework v4 — Modernização Jakarta EE 10

O Demoiselle Framework v4 foi modernizado para aproveitar plenamente os recursos do **Jakarta EE 10**, **CDI 4.0** e **Java 17**. Esta documentação cobre todas as funcionalidades introduzidas, organizadas em 17 áreas de implementação.

## Visão Geral das Mudanças

| Prioridade | Funcionalidade | Módulos |
|:---:|---|---|
| P1 | [Java 17 Records para DTOs](#p1--java-17-records-para-dtos) | crud, rest, configuration |
| P2 | [Sealed Classes para Filtros CRUD](#p2--sealed-classes-para-filtros-crud) | crud |
| P3 | [CDI 4.0 Lite Build-Compatible Extensions](#p3--cdi-40-lite-build-compatible-extensions) | core, configuration |
| P4 | [Coleções Imutáveis no Módulo de Segurança](#p4--coleções-imutáveis-no-módulo-de-segurança) | security, crud |
| P5 | [Preparação para Virtual Threads](#p5--preparação-para-virtual-threads) | configuration, script |
| P6 | [Melhorias na Criteria API do JPA 3.1](#p6--melhorias-na-criteria-api-do-jpa-31) | crud |
| P7 | [Soft Delete com @SoftDeletable](#p7--soft-delete-com-softdeletable) | crud |
| P8 | [Auditoria Automática](#p8--auditoria-automática) | crud |
| P9 | [Specification Pattern](#p9--specification-pattern) | crud |
| P10 | [Operações em Batch](#p10--operações-em-batch) | crud |
| P11 | [PageResult Tipado](#p11--pageresult-tipado) | crud |
| P12 | [Operadores de Comparação no FilterOp](#p12--operadores-de-comparação-no-filterop) | crud |
| P13 | [Cache de Consultas com Eventos CDI](#p13--cache-de-consultas-com-eventos-cdi) | crud |
| P14 | [Módulo de Observabilidade](#p14--módulo-de-observabilidade-demoiselle-observability) | observability |
| P15 | [Módulo OpenAPI](#p15--módulo-openapi-demoiselle-openapi) | openapi |
| P16 | [CI/CD com GitHub Actions](#p16--cicd-com-github-actions) | ci/cd |
| P17 | [Testes de Integração entre Módulos](#p17--módulo-de-testes-de-integração-demoiselle-integration-tests) | integration-tests |

---

## P1 — Java 17 Records para DTOs

### SortModel

O `SortModel` agora é um **record Java 17** imutável com validação no construtor compacto.

```java
// Antes (classe mutável com boilerplate)
SortModel sort = new SortModel(CrudSort.ASC, "name");
sort.getType();  // getter tradicional
sort.getField();

// Depois (record imutável)
SortModel sort = new SortModel(CrudSort.ASC, "name");
sort.type();   // accessor do record
sort.field();
```

**Validações automáticas:**

```java
// NullPointerException — tipo nulo
new SortModel(null, "name");

// NullPointerException — campo nulo
new SortModel(CrudSort.ASC, null);

// IllegalArgumentException — campo vazio ou em branco
new SortModel(CrudSort.ASC, "   ");
```

O `equals()` e `hashCode()` são gerados automaticamente pelo compilador com base nos componentes do record.

### DemoiselleRestExceptionMessage

Mensagens de erro REST agora são records imutáveis com nomenclatura Java padrão:

```java
// Criação de mensagem de erro
var msg = new DemoiselleRestExceptionMessage(
    "AUTH_FAILED",
    "Token expirado",
    "https://docs.example.com/errors/auth-failed"
);

// Acessores do record
msg.error();            // "AUTH_FAILED"
msg.errorDescription(); // "Token expirado"
msg.errorLink();        // pode ser null
```

**Serialização JSON** funciona nativamente com Jackson e JSON-B:

```json
{
  "error": "AUTH_FAILED",
  "errorDescription": "Token expirado",
  "errorLink": "https://docs.example.com/errors/auth-failed"
}
```

> **Migração:** Os campos foram renomeados de `error_description` → `errorDescription` e `error_link` → `errorLink` para seguir convenções Java.

### ResultSet com List.copyOf()

O `ResultSet` agora usa **cópias defensivas** via `List.copyOf()`:

```java
ResultSet rs = new ResultSet();

// setContent(null) resulta em lista vazia, não NullPointerException
rs.setContent(null);
rs.getContent(); // → List.of() (lista vazia imutável)

// Cópia defensiva — modificações na lista original não afetam o ResultSet
List<String> original = new ArrayList<>(List.of("a", "b", "c"));
rs.setContent(original);
original.add("d");           // modifica a lista original
rs.getContent().size();       // → 3 (inalterado)
```

### Records de Metadados no ConfigurationLoader

Dois records internos encapsulam metadados de configuração:

```java
// Metadados de campo de configuração
record ConfigFieldMeta(
    String key,          // chave no arquivo de configuração
    Field field,         // campo refletido
    boolean ignored,     // @ConfigurationIgnore presente?
    boolean suppressLog  // @ConfigurationSuppressLogger presente?
) {}

// Metadados da fonte de configuração
record ConfigSourceMeta(
    ConfigurationType type,  // PROPERTIES, XML ou SYSTEM
    String resource,         // nome do arquivo
    String prefix            // prefixo das chaves
) {}
```

---

## P2 — Sealed Classes para Filtros CRUD

### FilterOp — Hierarquia Selada

A nova `sealed interface FilterOp` substitui a cascata de `if-else` no `AbstractDAO` por uma hierarquia type-safe com **7 variantes**:

```java
public sealed interface FilterOp {
    String key();

    record Equals(String key, String value)          implements FilterOp {}
    record Like(String key, String pattern)           implements FilterOp {}
    record IsNull(String key)                         implements FilterOp {}
    record IsTrue(String key)                         implements FilterOp {}
    record IsFalse(String key)                        implements FilterOp {}
    record EnumFilter(String key, String value, int ordinal) implements FilterOp {}
    record UUIDFilter(String key, UUID value)         implements FilterOp {}
}
```

**Cada variante valida seus dados no construtor compacto** — `key` nunca é null, `Like.pattern` nunca é null, `EnumFilter.ordinal` é ≥ 0, etc.

### Resolução Automática de Filtros

O `AbstractDAO` resolve automaticamente o tipo de filtro com base no valor recebido:

| Valor | FilterOp Resolvido |
|---|---|
| `null` ou `"null"` | `IsNull` |
| `*texto` ou `texto*` | `Like` |
| `"true"` / `"isTrue"` | `IsTrue` |
| `"false"` / `"isFalse"` | `IsFalse` |
| Campo enum | `EnumFilter` |
| Campo UUID | `UUIDFilter` |
| Qualquer outro | `Equals` |

### Pattern Matching Exaustivo

O `buildPredicate()` usa **switch com pattern matching** garantido pelo compilador:

```java
protected Predicate buildPredicate(FilterOp op, From<?, ?> from,
        CriteriaBuilder cb, CriteriaQuery<?> cq) {
    return switch (op) {
        case FilterOp.IsNull(var key)              -> cb.isNull(from.get(key));
        case FilterOp.Like(var key, var pattern)    -> buildLikePredicate(cb, cq, from, key, pattern);
        case FilterOp.IsTrue(var key)               -> cb.isTrue(from.get(key));
        case FilterOp.IsFalse(var key)              -> cb.isFalse(from.get(key));
        case FilterOp.EnumFilter(var key, _, var o) -> cb.equal(from.get(key), o);
        case FilterOp.UUIDFilter(var key, var uuid) -> cb.equal(from.get(key), uuid);
        case FilterOp.Equals(var key, var val)      -> cb.equal(from.get(key), val);
    };
    // Sem cláusula default — o compilador garante exaustividade!
}
```

> **Benefício:** Se uma nova variante for adicionada à sealed interface, o código **não compila** até que o switch seja atualizado.

---

## P3 — CDI 4.0 Lite Build-Compatible Extensions

### MessageBundleBuildCompatibleExtension

A extensão CDI que descobre interfaces `@MessageBundle` foi migrada para a API **Build-Compatible** do CDI 4.0 Lite:

```java
public class MessageBundleBuildCompatibleExtension
        implements BuildCompatibleExtension {

    @Discovery
    public void discovery(ScannedClasses scan) {
        // CDI 4.0 Lite descobre automaticamente
    }

    @Enhancement(types = Object.class,
                 withAnnotations = MessageBundle.class)
    public void collectMessageBundles(ClassInfo classInfo) {
        // Coleta interfaces @MessageBundle
    }

    @Synthesis
    public void registerBeans(SyntheticComponents syn) {
        // Registra beans sintéticos com proxy dinâmico
    }
}
```

**Como usar `@MessageBundle`** (sem mudanças para o desenvolvedor):

```java
@MessageBundle
public interface AppMessages {

    @MessageTemplate("{welcome}")
    String welcome();

    @MessageTemplate("{greeting}")
    String greeting(String name);
}
```

```properties
# AppMessages.properties
welcome=Bem-vindo ao sistema
greeting=Olá, %s!
```

```java
@Inject
@MessageBundle
AppMessages messages;

messages.welcome();           // "Bem-vindo ao sistema"
messages.greeting("João");    // "Olá, João!"
```

> **Compatibilidade:** A extensão portável original (`MessageBundleExtension`) é mantida como fallback para containers que não suportam CDI Lite.

### ConfigurationBuildCompatibleExtension

A descoberta de `ConfigurationValueExtractor` também foi migrada para Build-Compatible Extension:

```java
public class ConfigurationBuildCompatibleExtension
        implements BuildCompatibleExtension {

    @Discovery
    public void discovery(ScannedClasses scan) { }

    @Enhancement(types = ConfigurationValueExtractor.class)
    public void collectExtractors(ClassInfo classInfo) {
        // Coleta implementações de ConfigurationValueExtractor
    }

    @Synthesis
    public void registerExtractorRegistry(SyntheticComponents syn) {
        // Registra bean sintético ApplicationScoped com o cache de extractors
    }
}
```

**Benefícios das Build-Compatible Extensions:**
- Compatível com **GraalVM native image** (processamento em build-time)
- Startup mais rápido em ambientes CDI Lite
- API mais declarativa e menos propensa a erros

---

## P4 — Coleções Imutáveis no Módulo de Segurança

### DemoiselleUserImpl — Cópias Defensivas

Todos os getters de coleções no `DemoiselleUserImpl` agora retornam **cópias defensivas verdadeiramente imutáveis**:

```java
@Inject
DemoiselleUser user;

// getRoles() retorna cópia independente via List.copyOf()
List<String> roles = user.getRoles();
// Modificações internas posteriores NÃO afetam esta cópia

// getPermissions() retorna deep copy
Map<String, List<String>> perms = user.getPermissions();
// Cada lista de valores também é copiada via List.copyOf()

// getParams() retorna cópia independente via Map.copyOf()
Map<String, String> params = user.getParams();
```

**Diferença em relação à versão anterior:**

```java
// ANTES: Collections.unmodifiableList() — view mutável
// Se a lista interna mudasse, a view refletia a mudança
List<String> roles = user.getRoles(); // view
user.addRole("admin");
roles.contains("admin"); // true (!) — a view refletia a mudança

// DEPOIS: List.copyOf() — cópia defensiva
List<String> roles = user.getRoles(); // cópia
user.addRole("admin");
roles.contains("admin"); // false — a cópia é independente
```

**Validação de null em `addRole()`:**

```java
user.addRole(null); // → NullPointerException
```

---

## P5 — Preparação para Virtual Threads

### ConfigurationLoader — ReentrantReadWriteLock

O `ConfigurationLoader` substituiu `synchronized` por `ReentrantReadWriteLock` para compatibilidade com **Virtual Threads (Project Loom)**:

```java
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

public void load(final Object object, Class<?> baseClass) {
    // Leitura rápida — múltiplas threads leem simultaneamente
    rwLock.readLock().lock();
    try {
        if (isAlreadyLoaded(object)) return;
    } finally {
        rwLock.readLock().unlock();
    }

    // Escrita exclusiva — apenas uma thread por vez
    rwLock.writeLock().lock();
    try {
        // Double-checked locking
        if (!isAlreadyLoaded(object)) {
            processConfiguration(object, baseClass);
        }
    } finally {
        rwLock.writeLock().unlock();
    }
}
```

**Benefícios:**
- Virtual threads **não ficam pinned** ao carrier thread durante espera
- Múltiplas leituras simultâneas para configurações já carregadas
- Escrita exclusiva apenas no primeiro carregamento
- Recuperação automática após exceções (retry habilitado)

### DynamicManagerCache — Campos de Instância

O `DynamicManagerCache` eliminou campos `static` mutáveis em favor de **campos de instância gerenciados pelo CDI**:

```java
@ApplicationScoped
public class DynamicManagerCache implements Serializable {

    // ANTES: static Map (compartilhado globalmente, não GC-friendly)
    // DEPOIS: campos de instância (ciclo de vida gerenciado pelo CDI)
    private final Map<String, ConcurrentHashMap<String, Object>> scriptCache =
        new ConcurrentHashMap<>();
    private final Map<String, Object> engineList =
        new ConcurrentHashMap<>();

    public Map<String, ConcurrentHashMap<String, Object>> getScriptCache() {
        return scriptCache;
    }

    public Map<String, Object> getEngineList() {
        return engineList;
    }
}
```

```java
// No DynamicManager — injeção via CDI
@Inject
private DynamicManagerCache cache;

// Uso via getters em vez de acesso estático
cache.getEngineList().put(engineName, engine);
cache.getScriptCache().put(engineName, new ConcurrentHashMap<>());
```

---

## P6 — Melhorias na Criteria API do JPA 3.1

### mergeHalf() com CriteriaUpdate

O `mergeHalf()` foi refatorado de **JPQL via StringBuilder** para **CriteriaUpdate type-safe**:

```java
@Override
public T mergeHalf(I id, T entity) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaUpdate<T> update = cb.createCriteriaUpdate(entityClass);
    Root<T> root = update.from(entityClass);

    boolean hasUpdates = false;

    for (final Field field : getAllFields(entityClass)) {
        // @ManyToOne sempre incluído
        if (!field.isAnnotationPresent(ManyToOne.class)) {
            final Column column = field.getAnnotation(Column.class);
            // Sem @Column ou @Column(updatable=false) → pular
            if (column == null || !column.updatable()) {
                continue;
            }
        }
        field.setAccessible(true);
        final Object value = field.get(entity);
        if (value != null) {
            update.set(root.get(field.getName()), value);
            hasUpdates = true;
        }
    }

    if (hasUpdates) {
        String idName = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);
        update.where(cb.equal(root.get(idName), id));
        getEntityManager().createQuery(update).executeUpdate();
    }

    return entity;
}
```

**Regras de inclusão de campos:**

| Anotação | Valor | Incluído no UPDATE? |
|---|---|:---:|
| `@Column(updatable=true)` | não-null | ✅ |
| `@Column(updatable=true)` | null | ❌ |
| `@Column(updatable=false)` | qualquer | ❌ |
| `@ManyToOne` | não-null | ✅ |
| `@ManyToOne` | null | ❌ |
| Sem anotação | qualquer | ❌ |

> **Benefício:** Sem risco de SQL injection via concatenação de strings. Erros de nome de campo detectados em tempo de compilação com metamodel.

### EntityGraph no AbstractDAO.find()

Subclasses do `AbstractDAO` agora podem controlar a **estratégia de fetch** via `EntityGraph`:

```java
public class PedidoDAO extends AbstractDAO<Pedido, Long> {

    @Override
    protected EntityGraph<Pedido> getEntityGraph() {
        EntityGraph<Pedido> graph = getEntityManager()
            .createEntityGraph(Pedido.class);
        graph.addAttributeNodes("itens", "cliente");
        return graph;
    }
}
```

Quando `getEntityGraph()` retorna não-null, o hint `jakarta.persistence.fetchgraph` é aplicado automaticamente na `TypedQuery`:

```java
TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);

EntityGraph<T> graph = getEntityGraph();
if (graph != null) {
    query.setHint("jakarta.persistence.fetchgraph", graph);
}
```

> **Comportamento padrão:** `getEntityGraph()` retorna `null` — nenhum hint é aplicado e o comportamento existente de paginação, ordenação e filtragem é mantido.

---

## P7 — Soft Delete com @SoftDeletable

Exclusão lógica declarativa via anotação. Em vez de `DELETE` físico, o framework executa um `UPDATE` marcando o registro como excluído.

### Configuração na Entidade

```java
@Entity
@SoftDeletable(field = "deletedAt")
public class Produto {

    @Id @GeneratedValue
    private Long id;

    private String nome;

    private LocalDateTime deletedAt; // campo de soft delete

    // getters e setters
}
```

### Tipos Suportados

```java
// LocalDateTime (padrão)
@SoftDeletable(field = "deletedAt")

// Boolean
@SoftDeletable(field = "deleted", type = Boolean.class)

// Instant
@SoftDeletable(field = "deletedInstant", type = Instant.class)
```

### Comportamento Automático no AbstractDAO

```java
// remove() executa UPDATE em vez de DELETE
dao.remove(42L);
// SQL gerado: UPDATE produto SET deleted_at = '2026-04-01T10:30:00' WHERE id = 42

// find() exclui registros soft-deleted automaticamente
Result result = dao.find();
// SQL gerado: SELECT ... FROM produto WHERE deleted_at IS NULL

// find(id) retorna null para registros soft-deleted
Produto p = dao.find(42L); // → null (registro marcado como excluído)

// count() exclui registros soft-deleted
Long total = dao.count();
// SQL gerado: SELECT COUNT(*) FROM produto WHERE deleted_at IS NULL

// findIncludingDeleted() retorna TODOS os registros
Result todos = dao.findIncludingDeleted();
// SQL gerado: SELECT ... FROM produto (sem filtro de soft delete)
```

### Filtro para Boolean

Quando o tipo é `Boolean`, o filtro é `WHERE deleted = false OR deleted IS NULL`:

```java
@SoftDeletable(field = "deleted", type = Boolean.class)
public class Tarefa {
    private Boolean deleted;
}

// remove() → UPDATE tarefa SET deleted = true WHERE id = ?
// find()   → SELECT ... WHERE (deleted = false OR deleted IS NULL)
```

### Validação na Inicialização

```java
// Campo inexistente → DemoiselleCrudException na inicialização do DAO
@SoftDeletable(field = "campoInexistente")
public class Invalida { }

// Tipo não suportado → DemoiselleCrudException
@SoftDeletable(field = "nome", type = String.class)
public class TipoInvalido { }
```

---

## P8 — Auditoria Automática

Preenchimento automático de campos de auditoria via JPA EntityListener, sem código manual em cada entidade.

### Anotações Disponíveis

| Anotação | Evento | Valor |
|---|---|---|
| `@CreatedAt` | `@PrePersist` | `LocalDateTime.now()` |
| `@CreatedBy` | `@PrePersist` | `DemoiselleUser.getIdentity()` |
| `@UpdatedAt` | `@PreUpdate` | `LocalDateTime.now()` |
| `@UpdatedBy` | `@PreUpdate` | `DemoiselleUser.getIdentity()` |

### Configuração na Entidade

```java
@Entity
@EntityListeners(AuditEntityListener.class)
public class Pedido {

    @Id @GeneratedValue
    private Long id;

    private String descricao;

    @CreatedAt
    private LocalDateTime criadoEm;

    @CreatedBy
    private String criadoPor;

    @UpdatedAt
    private LocalDateTime atualizadoEm;

    @UpdatedBy
    private String atualizadoPor;

    // getters e setters
}
```

### Comportamento

```java
// Ao persistir: preenche apenas @CreatedAt e @CreatedBy
entityManager.persist(pedido);
// pedido.criadoEm    → 2026-04-01T10:30:00
// pedido.criadoPor   → "admin"
// pedido.atualizadoEm → null
// pedido.atualizadoPor → null

// Ao atualizar: preenche apenas @UpdatedAt e @UpdatedBy
entityManager.merge(pedido);
// pedido.criadoEm    → (inalterado)
// pedido.criadoPor   → (inalterado)
// pedido.atualizadoEm → 2026-04-01T11:00:00
// pedido.atualizadoPor → "editor"
```

### Fallback sem Contexto de Usuário

Quando `DemoiselleUser` não está disponível (ex.: operações em batch sem requisição HTTP), o valor de `@CreatedBy`/`@UpdatedBy` é `"system"`.

---

## P9 — Specification Pattern

Composição declarativa de consultas JPA complexas sem escrever `CriteriaQuery` manualmente.

### Interface Funcional

```java
@FunctionalInterface
public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    // Métodos default para composição
    default Specification<T> and(Specification<T> other) { ... }
    default Specification<T> or(Specification<T> other)  { ... }
    default Specification<T> not()                        { ... }
}
```

### Criando Specifications

```java
// Specification simples
Specification<Produto> precoMaior100 = (root, query, cb) ->
    cb.greaterThan(root.get("preco"), 100.0);

Specification<Produto> categoriaEletronicos = (root, query, cb) ->
    cb.equal(root.get("categoria"), "ELETRONICOS");

Specification<Produto> emEstoque = (root, query, cb) ->
    cb.greaterThan(root.get("estoque"), 0);
```

### Composição

```java
// AND: produtos eletrônicos com preço > 100
Specification<Produto> filtro = categoriaEletronicos.and(precoMaior100);

// OR: eletrônicos OU preço > 100
Specification<Produto> filtroOu = categoriaEletronicos.or(precoMaior100);

// NOT: produtos que NÃO são eletrônicos
Specification<Produto> naoEletronicos = categoriaEletronicos.not();

// Composição complexa: eletrônicos em estoque OU preço > 100
Specification<Produto> complexo = categoriaEletronicos.and(emEstoque)
                                                       .or(precoMaior100);
```

### Uso no AbstractDAO

```java
// find(spec) combina Specification com filtros do DemoiselleRequestContext
Result result = dao.find(precoMaior100.and(emEstoque));

// Paginação é aplicada automaticamente quando habilitada
// Retorna PageResult com metadados quando paginação está ativa

// find(null) equivale a find() padrão
Result result = dao.find(null); // mesmo que dao.find()
```

---

## P10 — Operações em Batch

Processamento eficiente de grandes volumes com flush/clear automático para evitar estouro de memória.

### Configuração

```properties
# demoiselle.properties
demoiselle.crud.batch.size=100
```

Valor padrão: `50` registros por batch.

### persistAll

```java
List<Produto> produtos = List.of(
    new Produto("Notebook", 3500.0),
    new Produto("Mouse", 89.90),
    new Produto("Teclado", 199.90)
);

// Persiste todos com flush/clear a cada N registros
List<Produto> persistidos = dao.persistAll(produtos);
// persistidos.size() == 3
```

Em caso de erro, a exceção inclui o índice da entidade que falhou:

```java
try {
    dao.persistAll(entidades);
} catch (DemoiselleCrudException e) {
    // "Erro ao persistir entidade no índice 42"
    e.getMessage();
    e.getCause(); // PersistenceException original
}
```

### removeAll

```java
List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L);

// Remove todos (respeita @SoftDeletable quando presente)
int removidos = dao.removeAll(ids);
// removidos == 5
```

### updateAll

```java
// Atualiza todos os produtos da categoria "ELETRONICOS" com desconto
Specification<Produto> eletronicos = (root, query, cb) ->
    cb.equal(root.get("categoria"), "ELETRONICOS");

Map<String, Object> updates = Map.of(
    "desconto", 0.15,
    "emPromocao", true
);

int atualizados = dao.updateAll(eletronicos, updates);
// SQL: UPDATE produto SET desconto = 0.15, em_promocao = true
//      WHERE categoria = 'ELETRONICOS'
```

---

## P11 — PageResult Tipado

Record imutável com metadados completos de paginação, eliminando cálculos manuais no frontend.

### Estrutura

```java
public record PageResult<T>(
    List<T> content,       // conteúdo da página
    long totalElements,    // total de elementos em todas as páginas
    int totalPages,        // total de páginas
    int currentPage,       // página atual (0-indexed)
    int pageSize,          // tamanho da página
    boolean hasNext,       // existe próxima página?
    boolean hasPrevious    // existe página anterior?
) implements Result { }
```

### Uso Automático

O `AbstractDAO.find()` retorna `PageResult` automaticamente quando a paginação está habilitada:

```java
// Com paginação habilitada → PageResult
Result result = dao.find();
if (result instanceof PageResult<?> page) {
    page.totalElements(); // 150
    page.totalPages();    // 15
    page.currentPage();   // 0
    page.pageSize();      // 10
    page.hasNext();       // true
    page.hasPrevious();   // false
    page.content();       // List<Produto> (10 itens)
}

// Sem paginação → ResultSet (comportamento anterior mantido)
```

### Factory Method

```java
// Criação manual com cálculos automáticos de metadados
PageResult<Produto> page = PageResult.of(
    produtos,        // conteúdo
    150L,            // totalElements
    20,              // offset
    10               // pageSize
);
// totalPages = 15, currentPage = 2, hasNext = true, hasPrevious = true
```

### Headers HTTP

Quando a resposta contém `PageResult`, o `CrudFilter` inclui headers automaticamente:

```
HTTP/1.1 200 OK
X-Total-Count: 150
X-Total-Pages: 15
X-Current-Page: 0
X-Page-Size: 10
X-Has-Next: true
X-Has-Previous: false
Content-Range: ...
Access-Control-Expose-Headers: ..., X-Total-Count, X-Total-Pages, ...
```

---

## P12 — Operadores de Comparação no FilterOp

6 novos operadores de comparação via query string, sem necessidade de endpoints customizados.

### Novos Operadores

| Prefixo | Operador | Exemplo Query String | FilterOp |
|---|---|---|---|
| `gt:` | Maior que | `?preco=gt:100` | `GreaterThan` |
| `lt:` | Menor que | `?preco=lt:50` | `LessThan` |
| `gte:` | Maior ou igual | `?idade=gte:18` | `GreaterThanOrEqual` |
| `lte:` | Menor ou igual | `?estoque=lte:10` | `LessThanOrEqual` |
| `between:` | Entre dois valores | `?preco=between:10,100` | `Between` |
| `in:` | Lista de valores | `?status=in:ATIVO,PENDENTE` | `In` |

### Exemplos de Uso via API REST

```
GET /api/produtos?preco=gt:100
GET /api/produtos?preco=between:50,200
GET /api/produtos?categoria=in:ELETRONICOS,INFORMATICA,GAMES
GET /api/produtos?estoque=lte:5
GET /api/produtos?dataCriacao=gte:2026-01-01
```

### Precedência

Prefixos de operador têm precedência sobre filtros existentes. Um valor como `gt:*texto*` é interpretado como `GreaterThan` (não `Like`):

```
?campo=gt:true    → GreaterThan (não IsTrue)
?campo=lt:*abc*   → LessThan (não Like)
?campo=in:null    → In com valor "null" (não IsNull)
```

### Validação

```
# between: requer exatamente 2 valores
?preco=between:10        → IllegalArgumentException
?preco=between:10,20,30  → IllegalArgumentException
?preco=between:10,20     → OK (Between com lower=10, upper=20)
```

### Hierarquia Completa do FilterOp (13 variantes)

```java
public sealed interface FilterOp {
    // Originais (7)
    record Equals(String key, String value)                    implements FilterOp {}
    record Like(String key, String pattern)                    implements FilterOp {}
    record IsNull(String key)                                  implements FilterOp {}
    record IsTrue(String key)                                  implements FilterOp {}
    record IsFalse(String key)                                 implements FilterOp {}
    record EnumFilter(String key, String value, int ordinal)   implements FilterOp {}
    record UUIDFilter(String key, UUID value)                  implements FilterOp {}

    // Novos operadores de comparação (6)
    record GreaterThan(String key, String value)               implements FilterOp {}
    record LessThan(String key, String value)                  implements FilterOp {}
    record GreaterThanOrEqual(String key, String value)        implements FilterOp {}
    record LessThanOrEqual(String key, String value)           implements FilterOp {}
    record Between(String key, String lower, String upper)     implements FilterOp {}
    record In(String key, List<String> values)                 implements FilterOp {}
}
```

---

## P13 — Cache de Consultas com Eventos CDI

Cache automático de resultados de consultas com invalidação reativa via eventos CDI.

### Habilitando Cache em um Método

```java
public class ProdutoREST extends AbstractREST<Produto, Long> {

    @GET
    @Cacheable(ttl = 60) // cache por 60 segundos
    public Result find() {
        return super.find();
    }

    @GET
    @Path("/destaque")
    @Cacheable // TTL padrão: 300 segundos (5 minutos)
    public Result findDestaques() {
        // consulta customizada
    }
}
```

### Como Funciona

1. Requisição GET chega ao endpoint `@Cacheable`
2. `CacheInterceptor` verifica se existe resultado em cache para a chave `entityClass:method:paramsHash`
3. Cache hit → retorna resultado imediatamente (header `X-Cache: HIT`)
4. Cache miss → executa o método, armazena resultado no cache (header `X-Cache: MISS`)

### Invalidação Automática

Operações de escrita no `AbstractDAO` disparam `EntityModifiedEvent` automaticamente:

```java
// persist() → EntityModifiedEvent(Produto.class, PERSIST, entity)
// mergeFull() / mergeHalf() → EntityModifiedEvent(Produto.class, MERGE, entity)
// remove() → EntityModifiedEvent(Produto.class, REMOVE, id)
```

O `CacheInvalidationListener` observa esses eventos e invalida todas as entradas de cache da classe afetada:

```java
// Ao persistir um Produto, TODAS as consultas cacheadas de Produto são invalidadas
// Consultas cacheadas de outras entidades (Pedido, Cliente) permanecem intactas
```

### Headers de Cache na Resposta

```
# Cache miss (primeira requisição)
HTTP/1.1 200 OK
X-Cache: MISS

# Cache hit (requisições subsequentes dentro do TTL)
HTTP/1.1 200 OK
X-Cache: HIT
```

### Arquitetura do Cache

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  CrudFilter │────▶│  CacheInterceptor │────▶│ QueryCacheStore  │
│  (JAX-RS)   │     │  (CDI Interceptor)│     │ (ConcurrentMap)  │
└─────────────┘     └──────────────────┘     └─────────────────┘
                                                       ▲
                                                       │ invalidate
                                              ┌────────┴────────┐
                                              │ CacheInvalidation│
                                              │    Listener      │
                                              └────────┬────────┘
                                                       │ @Observes
                                              ┌────────┴────────┐
                                              │EntityModifiedEvent│
                                              └────────┬────────┘
                                                       │ fire()
                                              ┌────────┴────────┐
                                              │   AbstractDAO    │
                                              │ persist/merge/   │
                                              │    remove        │
                                              └─────────────────┘
```

---

## P14 — Módulo de Observabilidade (`demoiselle-observability`)

Módulo transversal que fornece métricas, health checks e tracing distribuído para os demais módulos do framework. Todas as dependências externas (MicroProfile Metrics, MicroProfile Health, OpenTelemetry) são opcionais — quando ausentes, o módulo degrada graciosamente para implementações noop sem erro.

### Dependência Maven

```xml
<dependency>
    <groupId>org.demoiselle.jee</groupId>
    <artifactId>demoiselle-observability</artifactId>
</dependency>
```

### @Counted — Métricas via CDI Interceptor

Incrementa automaticamente um contador MicroProfile Metrics a cada invocação do método anotado.

```java
import org.demoiselle.jee.observability.annotation.Counted;

@ApplicationScoped
public class TokenService {

    @Counted("demoiselle.jwt.tokens.issued")
    public String issueToken(DemoiselleUser user) {
        // lógica de emissão de token
        return jwt;
    }

    @Counted // nome automático: "TokenService.validateToken"
    public DemoiselleUser validateToken(String jwt) {
        // lógica de validação
        return user;
    }
}
```

Contadores pré-definidos do framework:

| Módulo | Operação | Contador |
|---|---|---|
| security-jwt | Token emitido | `demoiselle.jwt.tokens.issued` |
| security-jwt | Token validado | `demoiselle.jwt.tokens.validated` |
| rest | Rate limit rejeitado | `demoiselle.rest.ratelimit.rejected` |
| configuration | Configuração carregada | `demoiselle.configuration.loaded` |
| script | Script executado | `demoiselle.script.executed` |

Quando `value()` está vazio, o nome é gerado automaticamente no formato `Classe.metodo`.

### @Traced — Tracing OpenTelemetry via CDI Interceptor

Cria spans OpenTelemetry automaticamente com atributos `demoiselle.module` e `demoiselle.operation`.

```java
import org.demoiselle.jee.observability.annotation.Traced;

@ApplicationScoped
public class PedidoService {

    @Traced(module = "pedido", operation = "criar")
    public Pedido criarPedido(PedidoDTO dto) {
        // lógica de criação
        return pedido;
    }

    @Traced // module = "PedidoService", operation = "buscarPorId"
    public Pedido buscarPorId(Long id) {
        return pedido;
    }
}
```

Quando `module()` ou `operation()` estão vazios, os valores são derivados do nome da classe e do método.

### MetricsAdapter — Abstração para Degradação Graceful

```java
// Interface
public interface MetricsAdapter {
    void increment(String counterName);
    long getCount(String counterName);
}

// Quando MicroProfile Metrics está no classpath:
//   → MicroProfileMetricsAdapter (delega para MetricRegistry)

// Quando MicroProfile Metrics NÃO está no classpath:
//   → NoopMetricsAdapter (increment() não faz nada, getCount() retorna 0)
```

### TracingAdapter — Abstração para Degradação Graceful

```java
public interface TracingAdapter {
    <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception;

    @FunctionalInterface
    interface SpanCallable<T> {
        T call() throws Exception;
    }
}

// Quando OpenTelemetry está no classpath:
//   → OpenTelemetryTracingAdapter (cria spans com atributos)

// Quando OpenTelemetry NÃO está no classpath:
//   → NoopTracingAdapter (executa callable diretamente)
```

### Health Checks — MicroProfile Health

O módulo registra automaticamente health checks quando MicroProfile Health está no classpath:

```
GET /health/live
{
  "status": "UP",
  "checks": [
    { "name": "demoiselle-cdi", "status": "UP" }
  ]
}

GET /health/ready
{
  "status": "UP",
  "checks": [
    { "name": "demoiselle-configuration", "status": "UP", "data": { "module": "demoiselle-configuration" } },
    { "name": "demoiselle-security-jwt-keys", "status": "UP", "data": { "type": "master", "publicKeyConfigured": true } }
  ]
}
```

Health checks registrados:

| Nome | Tipo | Verifica |
|---|---|---|
| `demoiselle-cdi` | Liveness | CDI container ativo |
| `demoiselle-configuration` | Readiness | Configuração carregada |
| `demoiselle-security-jwt-keys` | Readiness | Chaves JWT disponíveis (quando `demoiselle-security-jwt` no classpath) |

### ObservabilityExtension — Detecção Automática de APIs

A CDI Extension detecta automaticamente quais APIs estão no classpath e registra os beans apropriados:

```
[INFO] MicroProfile Metrics detected — registering MicroProfileMetricsAdapter
[INFO] MicroProfile Health detected — registering HealthCheckProducer
[INFO] OpenTelemetry não disponível — tracing desativado
```

Nenhuma configuração manual é necessária. Basta adicionar a dependência da API desejada ao `pom.xml`:

```xml
<!-- Para métricas -->
<dependency>
    <groupId>org.eclipse.microprofile.metrics</groupId>
    <artifactId>microprofile-metrics-api</artifactId>
</dependency>

<!-- Para health checks -->
<dependency>
    <groupId>org.eclipse.microprofile.health</groupId>
    <artifactId>microprofile-health-api</artifactId>
</dependency>

<!-- Para tracing -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
</dependency>
```

---

## P15 — Módulo OpenAPI (`demoiselle-openapi`)

Geração automática de documentação OpenAPI para endpoints do framework via MicroProfile OpenAPI.

### Dependência Maven

```xml
<dependency>
    <groupId>org.demoiselle.jee</groupId>
    <artifactId>demoiselle-openapi</artifactId>
</dependency>
```

### OpenAPIContributor — Interface para Contribuições Modulares

Cada módulo do framework pode contribuir definições OpenAPI parciais implementando esta interface:

```java
import org.demoiselle.jee.openapi.OpenAPIContributor;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;

@ApplicationScoped
public class MeuModuloOpenAPIContributor implements OpenAPIContributor {

    @Override
    public OpenAPI contribute() {
        OpenAPI partial = OASFactory.createOpenAPI();
        partial.paths(OASFactory.createPaths()
            .addPathItem("/api/meu-recurso", OASFactory.createPathItem()
                .GET(OASFactory.createOperation()
                    .summary("Lista recursos")
                    .operationId("listarRecursos"))));
        return partial;
    }
}
```

### DemoiselleOASModelReader — Agregação Automática

O `DemoiselleOASModelReader` descobre todos os `OpenAPIContributor` via CDI e agrega suas contribuições:

- Paths de múltiplos contributors são mesclados no documento final
- Em caso de sobreposição, o primeiro contributor processado prevalece (first-wins)
- Se um contributor lançar exceção, o erro é logado e os demais continuam normalmente

### Configuração de Ativação

```properties
# demoiselle.properties
# Desativar documentação OpenAPI automática (default: true)
demoiselle.openapi.enabled=false
```

Quando desativado, `DemoiselleOASModelReader.buildModel()` retorna um documento OpenAPI vazio.

---

## P16 — CI/CD com GitHub Actions

O pipeline de CI/CD foi migrado do Travis CI para GitHub Actions com build matrix multi-versão.

### Build Matrix Java 17/21

O workflow executa build e testes em Java 17 e 21 simultaneamente:

```yaml
# .github/workflows/ci.yml
strategy:
  fail-fast: false
  matrix:
    java-version: ['17', '21']
```

- `fail-fast: false` — falha em uma versão não cancela a outra
- Cache Maven via `actions/setup-java` com `cache: 'maven'`
- Relatórios JaCoCo como artefatos separados por versão de Java

### Relatórios de Cobertura em PRs

Em pull requests, um job separado gera relatório agregado de cobertura e posta um resumo no PR:

```
📊 JaCoCo Coverage Summary

| Module                  | Instruction | Branch | Line  | Method |
|-------------------------|------------|--------|-------|--------|
| demoiselle-core         | 85.2%      | 72.1%  | 83.4% | 90.1%  |
| demoiselle-configuration| 78.5%      | 65.3%  | 76.2% | 85.7%  |
| demoiselle-security     | 91.0%      | 80.4%  | 89.1% | 93.2%  |
```

### Limiar de Cobertura Configurável

```xml
<!-- pom.xml raiz -->
<properties>
    <jacoco.minimum.coverage>0.00</jacoco.minimum.coverage>
</properties>
```

O build falha quando a cobertura de um módulo cai abaixo do limiar:

```
Rule violated for bundle demoiselle-core: instructions covered ratio is 0.71,
but expected minimum is 0.80
```

Pode ser sobrescrito via linha de comando: `mvn verify -Djacoco.minimum.coverage=0.80`

---

## P17 — Módulo de Testes de Integração (`demoiselle-integration-tests`)

Módulo dedicado a testes de integração que validam fluxos completos entre múltiplos módulos do framework.

### Estrutura

- Executado na fase `verify` do Maven via `maven-failsafe-plugin`
- Testes unitários (`surefire`) são desabilitados neste módulo
- Módulos opcionais são tratados com `@EnabledIf` do JUnit 5

### Fluxo Configuração → Segurança → REST

```java
@EnabledIf("isSecurityJwtAvailable")
class ConfigSecurityRestIT {

    @Test
    void fullFlow_configLoadAndTokenIssueAndValidate() {
        // 1. Carregar configuração de segurança
        // 2. Emitir token JWT com claims
        // 3. Validar token em novo TokenManager
        // 4. Verificar que claims são preservados
    }

    @Test
    void requiredRole_acceptsValidTokenWithCorrectRole() {
        // Token com role "admin" → interceptor permite execução
    }

    @Test
    void requiredRole_rejectsTokenWithWrongRole() {
        // Token com role "viewer" → interceptor rejeita com 403
    }

    @Test
    void expiredToken_isRejectedWithUnauthorized() {
        // Token expirado → interceptor rejeita com 401
    }

    @Test
    void corsFilter_appliesConfiguredHeaders() {
        // Filtro CORS aplica headers configurados
    }
}
```

### Fluxo Configuração → Script

```java
@EnabledIf("isScriptAvailable")
class ConfigScriptIT {

    @Test
    void loadEngineAndExecuteScriptWithParameters() {
        // 1. Carregar engine Groovy
        // 2. Cachear script com parâmetros
        // 3. Executar e verificar resultado
    }

    @Test
    void fullFlow_loadCacheUpdateAndReExecute() {
        // Carregar → cachear → atualizar → re-executar
    }
}
```

### Tratamento de Módulos Opcionais

```java
@EnabledIf("isSecurityJwtAvailable")
class SecurityJwtIntegrationIT {

    static boolean isSecurityJwtAvailable() {
        try {
            Class.forName("org.demoiselle.jee.security.jwt.impl.TokenManagerImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
```

Quando o módulo não está no classpath, os testes são ignorados automaticamente (não falham).

---

## Testes Baseados em Propriedades (jqwik)

O framework inclui **39 property-based tests** usando [jqwik](https://jqwik.net/) que validam propriedades universais de corretude:

| # | Propriedade | Módulo |
|:---:|---|---|
| 1 | Rejeição de campos blank no SortModel | crud |
| 2 | Igualdade estrutural de Records | crud |
| 3 | Round-trip JSON do DemoiselleRestExceptionMessage | rest |
| 4 | Independência de cópia defensiva no ResultSet | crud |
| 5 | Cópias defensivas no DemoiselleUserImpl | security |
| 6 | Null-safety do FilterOp.key() (13 variantes) | crud |
| 7 | resolveFilterOp() sempre retorna FilterOp válido | crud |
| 8 | Wildcard resolve para Like | crud |
| 9 | Exclusão correta de campos no CriteriaUpdate | crud |
| 10 | Soft delete marca registro em vez de remover | crud |
| 11 | Consultas excluem registros soft-deleted | crud |
| 12 | findIncludingDeleted retorna todos os registros | crud |
| 13 | Persist preenche apenas campos de criação | crud |
| 14 | Merge preenche apenas campos de atualização | crud |
| 15 | Specification.and() retorna interseção | crud |
| 16 | Specification.or() retorna união | crud |
| 17 | Specification.not() retorna complemento | crud |
| 18 | find(Specification) combina spec com filtros DRC | crud |
| 19 | find(Specification) aplica paginação | crud |
| 20 | persistAll retorna lista de mesmo tamanho | crud |
| 21 | removeAll retorna contagem correta | crud |
| 22 | updateAll aplica updates e Specification | crud |
| 23 | PageResult tipo correto baseado em paginação | crud |
| 24 | PageResult calcula metadados corretamente | crud |
| 25 | PageResult cópia defensiva | crud |
| 26 | resolveFilterOp resolve prefixos de operador | crud |
| 27 | Prefixos de operador têm precedência | crud |
| 28 | Cache round-trip (hit/miss com TTL) | crud |
| 29 | Operações de escrita disparam EntityModifiedEvent | crud |
| 30 | Invalidação de cache por entityClass | crud |
| 31 | Contagem monotônica do @Counted | observability |
| 32 | Segurança dos adapters noop | observability |
| 33 | Span do @Traced contém atributos corretos | observability |
| 34 | Agregação de OpenAPIContributors preserva paths | openapi |
| 35 | Tolerância a falhas na agregação OpenAPI | openapi |
| 36 | Interceptor de segurança aceita tokens válidos e rejeita inválidos | integration-tests |
| 37 | Round-trip de configuração | integration-tests |
| 38 | Round-trip de claims JWT | integration-tests |
| 39 | Invariante do rate limiter | integration-tests |

**Exemplo — Property test de cópia defensiva:**

```java
@Property(tries = 200)
void modifyingOriginalListDoesNotAffectGetContent(
        @ForAll List<String> elements) {

    List<String> mutableList = new ArrayList<>(elements);
    ResultSet rs = new ResultSet();
    rs.setContent(mutableList);

    List<?> snapshot = rs.getContent();

    mutableList.add("EXTRA");
    mutableList.clear();

    assertEquals(elements.size(), snapshot.size(),
        "Modificações na lista original não devem afetar getContent()");
}
```

---

## Compatibilidade

- **Java 17+** (records, sealed classes, pattern matching)
- **Jakarta EE 10** (CDI 4.0, JPA 3.1, JAX-RS 3.1)
- **Retrocompatível** com aplicações Demoiselle v4 existentes
- Cada prioridade pode ser adotada independentemente

## Requisitos Mínimos

| Componente | Versão |
|---|---|
| Java | 17+ |
| Jakarta EE | 10 |
| CDI | 4.0 |
| JPA | 3.1 |
| Weld (referência CDI) | 5.x |
