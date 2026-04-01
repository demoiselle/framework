---
layout: default
title: Demoiselle Framework v4 — Modernização Jakarta EE 10
---

# Demoiselle Framework v4 — Modernização Jakarta EE 10

O Demoiselle Framework v4 foi modernizado para aproveitar plenamente os recursos do **Jakarta EE 10**, **CDI 4.0** e **Java 17**. Esta documentação cobre todas as funcionalidades introduzidas, organizadas em 6 prioridades de implementação.

## Visão Geral das Mudanças

| Prioridade | Funcionalidade | Módulos |
|:---:|---|---|
| P1 | [Java 17 Records para DTOs](#p1--java-17-records-para-dtos) | crud, rest, configuration |
| P2 | [Sealed Classes para Filtros CRUD](#p2--sealed-classes-para-filtros-crud) | crud |
| P3 | [CDI 4.0 Lite Build-Compatible Extensions](#p3--cdi-40-lite-build-compatible-extensions) | core, configuration |
| P4 | [Coleções Imutáveis no Módulo de Segurança](#p4--coleções-imutáveis-no-módulo-de-segurança) | security, crud |
| P5 | [Preparação para Virtual Threads](#p5--preparação-para-virtual-threads) | configuration, script |
| P6 | [Melhorias na Criteria API do JPA 3.1](#p6--melhorias-na-criteria-api-do-jpa-31) | crud |

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

## Testes Baseados em Propriedades (jqwik)

O framework inclui **9 property-based tests** usando [jqwik](https://jqwik.net/) que validam propriedades universais de corretude:

| # | Propriedade | Módulo |
|:---:|---|---|
| 1 | Rejeição de campos blank no SortModel | crud |
| 2 | Igualdade estrutural de Records | crud |
| 3 | Round-trip JSON do DemoiselleRestExceptionMessage | rest |
| 4 | Independência de cópia defensiva no ResultSet | crud |
| 5 | Cópias defensivas no DemoiselleUserImpl | security |
| 6 | Null-safety do FilterOp.key() | crud |
| 7 | resolveFilterOp() sempre retorna FilterOp válido | crud |
| 8 | Wildcard resolve para Like | crud |
| 9 | Exclusão correta de campos no CriteriaUpdate | crud |

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
