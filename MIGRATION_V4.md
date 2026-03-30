# Demoiselle Framework — Atividades de Migração v3 → v4

## Resumo

Migração do Demoiselle Framework 3.0.6 (Java EE 7) para 4.0.0 (Jakarta EE 10+).

## Atividades de Migração

### 1. Namespace javax → jakarta
- Substituir todos os imports `javax.enterprise.*` → `jakarta.enterprise.*`
- Substituir todos os imports `javax.inject.*` → `jakarta.inject.*`
- Substituir todos os imports `javax.ws.rs.*` → `jakarta.ws.rs.*`
- Substituir todos os imports `javax.validation.*` → `jakarta.validation.*`
- Substituir todos os imports `javax.persistence.*` → `jakarta.persistence.*`
- Substituir todos os imports `javax.ejb.*` → `jakarta.ejb.*`
- Substituir todos os imports `javax.servlet.*` → `jakarta.servlet.*`
- Substituir todos os imports `javax.annotation.*` → `jakarta.annotation.*`
- Substituir todos os imports `javax.json.*` → `jakarta.json.*`
- Renomear `META-INF/services/javax.enterprise.inject.spi.Extension` → `META-INF/services/jakarta.enterprise.inject.spi.Extension`
- Atualizar namespaces em arquivos `beans.xml`

### 2. Java 8 → Java 17
- Atualizar `maven.compiler.source` e `maven.compiler.target` para 17
- Atualizar `java.version` para 17
- Usar `<release>17</release>` no maven-compiler-plugin

### 3. Atualização do BOM (dependências)
- `javax:javaee-api:7.0` → `jakarta.platform:jakarta.jakartaee-api:10.0.0`
- `javax.enterprise:cdi-api:1.2` → `jakarta.enterprise:jakarta.enterprise.cdi-api:4.0`
- `javax.ws.rs:javax.ws.rs-api:2.0.1` → `jakarta.ws.rs:jakarta.ws.rs-api:3.1`
- `javax.validation:validation-api:1.1.0.Final` → `jakarta.validation:jakarta.validation-api:3.0`
- `javax.persistence:persistence-api:1.0.2` → `jakarta.persistence:jakarta.persistence-api:3.1`
- `javax.ejb:javax.ejb-api:3.2` → `jakarta.ejb:jakarta.ejb-api:4.0`
- `javax.servlet:javax.servlet-api:3.1.0` → `jakarta.servlet:jakarta.servlet-api:6.0`
- `jose4j:0.5.2` → `jose4j:0.9.x+`
- `commons-configuration2:2.0` → `commons-configuration2:2.9.x+`
- `hibernate-validator:5.3.0.Final` → `hibernate-validator:8.0.x`
- `weld-se-core:2.4.0.Final` → `weld-se-core:5.x`
- `commons-lang3:3.3.2` → `commons-lang3:3.14.x+`
- `commons-beanutils:1.9.2` → `commons-beanutils:1.9.4+`
- Versão do framework: `3.0.6-SNAPSHOT` → `4.0.0-SNAPSHOT`

### 4. Swagger → OpenAPI 3.0
- Substituir `io.swagger:swagger-jaxrs:1.5.12` por MicroProfile OpenAPI / SmallRye OpenAPI
- Migrar anotações `@Api`, `@ApiOperation`, `@ApiParam` → `@Tag`, `@Operation`, `@Parameter`

### 5. Remoção do WildFly Swarm
- Remover profile `wildfly-swarm` do `demoiselle-parent-rest/pom.xml`
- Remover dependências `org.wildfly.swarm.*`
- Remover plugin `wildfly-swarm-plugin`
- Remover plugin `docker-maven-plugin` do Spotify
- Remover propriedade `wildfly-swarm.version` e import do BOM
- Documentar compatibilidade com WildFly 27+, Quarkus, Open Liberty

### 6. JUnit 4 → JUnit 5
- `@org.junit.Test` → `@org.junit.jupiter.api.Test`
- `@Before/@After` → `@BeforeEach/@AfterEach`
- `@BeforeClass/@AfterClass` → `@BeforeAll/@AfterAll`
- `@Ignore` → `@Disabled`
- `Assert.*` → `Assertions.*`
- `@RunWith` → `@ExtendWith`
- Spock 1.0 → Spock 2.x ou migrar para JUnit 5
- Configurar `maven-surefire-plugin` 3.x com JUnit Platform

### 7. Travis CI → GitHub Actions
- Criar `.github/workflows/ci.yml`
- Build com Java 17
- Testes em push/PR
- Relatórios JaCoCo
- Publicação Maven (Sonatype/OSSRH)
- Remover ou deprecar `.travis.yml`

### 8. Remoção do DeltaSpike
- Substituir message bundles DeltaSpike por implementação própria ou CDI 4.0 nativo
- Substituir test-control por Weld JUnit 5 Extension ou Arquillian Jakarta EE
- Remover todas as dependências `org.apache.deltaspike.*`

### 9. Correções Arquiteturais
- **AbstractDAO**: Substituir strings hardcoded em português por message bundles
- **TokenManagerImpl**: Mover chaves RSA estáticas para escopo `@ApplicationScoped`
- **TokenManagerImpl**: Corrigir `removeUser()` que lança `UnsupportedOperationException` (violação LSP)
- **CrudFilter**: Adicionar cache de reflection (metadados de Field por classe)
- **CrudFilter**: Ampliar projeção de campos para profundidade > 2 níveis (recursivo)
- **SecurityContextImpl**: Adicionar validação de null em `hasPermission()`, `hasRole()`, `isLoggedIn()`
- **DemoiselleUserImpl**: Substituir string hardcoded "Erro ao clonar" por message bundle
- **Tratamento de exceções**: Substituir `catch(Exception e)` genérico por exceções específicas

### 10. Atualização de Plugins Maven
- `maven-compiler-plugin` → 3.11+
- `maven-source-plugin` → 3.3+
- `maven-javadoc-plugin` → 3.6+
- `jacoco-maven-plugin` → 0.8.11+
- `maven-surefire-plugin` → 3.2+
- `maven-release-plugin` → 3.0+
- `sonar-maven-plugin` → 3.10+
- `nexus-staging-maven-plugin` → 1.6.13+

### 11. Módulo Script
- Atualizar Groovy 2.4.7 → Apache Groovy 4.x
- Manter `javax.script` (API do JDK, não migra para jakarta)
- Atualizar imports CDI/Jakarta

### 12. Cobertura e Qualidade
- Atualizar JaCoCo para 0.8.11+
- Remover token hardcoded do Coveralls
- Atualizar integração SonarQube para Java 17
- Substituir Coveralls por integração GitHub Actions

### 13. Versionamento e Documentação
- Bump de versão para 4.0.0-SNAPSHOT em todos os módulos
- Atualizar descrição do POM raiz (Jakarta EE 10, CDI 4.0)
- Atualizar README.md para versão 4
- Atualizar tags SCM

