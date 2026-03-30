# Guia de Migração — Demoiselle Framework v3 → v4

## Visão Geral

O Demoiselle 4 migra de Java EE 7 (`javax.*`) para Jakarta EE 10 (`jakarta.*`), com Java 17 como versão mínima. Este guia cobre os passos necessários para atualizar aplicações existentes.

## Pré-requisitos

- Java 17+ (LTS)
- Maven 3.9+
- Servidor de aplicação compatível com Jakarta EE 10 (WildFly 27+, Quarkus, Open Liberty)

## Passo 1: Atualizar o POM

Altere a versão do parent e dependências do Demoiselle:

```xml
<parent>
    <groupId>org.demoiselle.jee</groupId>
    <artifactId>demoiselle-parent</artifactId>
    <version>4.0.0-SNAPSHOT</version>
</parent>
```

Se usar `demoiselle-parent-rest`, atualize também:

```xml
<parent>
    <groupId>org.demoiselle.jee</groupId>
    <artifactId>demoiselle-parent-rest</artifactId>
    <version>4.0.0-SNAPSHOT</version>
</parent>
```

Atualize `maven-compiler-plugin` para Java 17:

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <release>17</release>
    </configuration>
</plugin>
```

## Passo 2: Migrar Imports javax → jakarta

Substitua todos os imports de APIs Jakarta EE:

| Antigo | Novo |
|---|---|
| `javax.enterprise.*` | `jakarta.enterprise.*` |
| `javax.inject.*` | `jakarta.inject.*` |
| `javax.ws.rs.*` | `jakarta.ws.rs.*` |
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.ejb.*` | `jakarta.ejb.*` |
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.annotation.*` | `jakarta.annotation.*` |
| `javax.json.*` | `jakarta.json.*` |

**Atenção**: `javax.script.*` (API do JDK) **não** deve ser alterado.

Comando para buscar imports que precisam ser migrados:

```bash
grep -rn "import javax\." --include="*.java" src/
```

## Passo 3: Atualizar beans.xml

Substitua o namespace nos arquivos `META-INF/beans.xml`:

```xml
<!-- Antes -->
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
       http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
       bean-discovery-mode="all">
</beans>

<!-- Depois -->
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
       https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0" bean-discovery-mode="all">
</beans>
```

## Passo 4: Renomear Arquivos SPI

Se sua aplicação registra CDI Extensions via `META-INF/services/`:

```
# Antes
META-INF/services/javax.enterprise.inject.spi.Extension

# Depois
META-INF/services/jakarta.enterprise.inject.spi.Extension
```

## Passo 5: Remover DeltaSpike

O Demoiselle 4 não depende mais do Apache DeltaSpike. Se sua aplicação usa DeltaSpike diretamente:

- `@MessageBundle` / `@MessageTemplate` → use as anotações próprias do Demoiselle em `org.demoiselle.jee.core.annotation`
- `CdiTestRunner` → substitua por Weld JUnit 5 (`@EnableAutoWeld`)

## Passo 6: Migrar Testes para JUnit 5

| JUnit 4 | JUnit 5 |
|---|---|
| `@org.junit.Test` | `@org.junit.jupiter.api.Test` |
| `@Before` | `@BeforeEach` |
| `@After` | `@AfterEach` |
| `@BeforeClass` | `@BeforeAll` |
| `@AfterClass` | `@AfterAll` |
| `@Ignore` | `@Disabled` |
| `@RunWith(CdiTestRunner.class)` | `@EnableAutoWeld` |
| `Assert.assertEquals(expected, actual)` | `Assertions.assertEquals(expected, actual)` |

Dependência de teste no POM:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.jboss.weld</groupId>
    <artifactId>weld-junit5</artifactId>
    <scope>test</scope>
</dependency>
```

## Passo 7: Remover WildFly Swarm

O profile `wildfly-swarm` foi removido. Para runtimes embarcados, use WildFly 27+ (Galleon), Quarkus ou Open Liberty.

## Passo 8: Migrar Swagger → OpenAPI

Se sua aplicação usa anotações Swagger 1.x:

| Swagger 1.x | OpenAPI 3.0 (MicroProfile) |
|---|---|
| `@Api` | `@Tag` |
| `@ApiOperation` | `@Operation` |
| `@ApiParam` | `@Parameter` |
| `io.swagger:swagger-jaxrs` | `org.eclipse.microprofile.openapi:microprofile-openapi-api` |

## Passo 9: Atualizar Groovy (se aplicável)

Se sua aplicação usa o módulo `demoiselle-script`:

```xml
<!-- Antes -->
<dependency>
    <groupId>org.codehaus.groovy</groupId>
    <artifactId>groovy-all</artifactId>
</dependency>

<!-- Depois -->
<dependency>
    <groupId>org.apache.groovy</groupId>
    <artifactId>groovy-all</artifactId>
    <type>pom</type>
</dependency>
```

## Mudanças de Comportamento

### SecurityContextImpl (null-safety)
- `hasPermission()`, `hasRole()` e `isLoggedIn()` agora retornam `false` quando o usuário não está autenticado, em vez de lançar `NullPointerException`.

### TokenManagerImpl (JWT)
- `removeUser()` agora limpa o token do escopo de request em vez de lançar `UnsupportedOperationException`.
- Chaves RSA são gerenciadas por `KeyPairHolder` (`@ApplicationScoped`) em vez de campos `static`.

### AbstractDAO
- Mensagens de erro agora vêm de message bundles (internacionalizáveis).
- Exceções específicas (`PersistenceException`) são capturadas em vez de `Exception` genérica.

### CrudFilter
- Projeção de campos agora suporta profundidade arbitrária (antes limitada a 2 níveis).
- Cache de reflection via `ReflectionCache` melhora performance em cenários de alta carga.

## Checklist de Migração

- [ ] Atualizar versão do parent POM para 4.0.0
- [ ] Configurar Java 17 no maven-compiler-plugin
- [ ] Substituir imports `javax.*` por `jakarta.*`
- [ ] Atualizar `beans.xml` para namespace Jakarta EE
- [ ] Renomear arquivos `META-INF/services/javax.*` para `jakarta.*`
- [ ] Remover dependências DeltaSpike
- [ ] Migrar testes de JUnit 4 para JUnit 5
- [ ] Remover configurações WildFly Swarm
- [ ] Migrar anotações Swagger para OpenAPI (se aplicável)
- [ ] Atualizar servidor de aplicação para Jakarta EE 10 compatível
- [ ] Testar a aplicação completa
