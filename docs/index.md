---
layout: portal
title: Início
description: Demoiselle é o framework integrador para construir produtos Java 21 e Jakarta EE 10 seguros, consistentes, observáveis e portáveis.
permalink: /
---

<section class="hero" aria-labelledby="hero-title">
  <div class="hero-glow hero-glow-one" aria-hidden="true"></div>
  <div class="hero-glow hero-glow-two" aria-hidden="true"></div>
  <div class="shell hero-grid">
    <div class="hero-copy reveal-target">
      <div class="release-badge"><span aria-hidden="true"></span> Ciclo 4.1.0-SNAPSHOT</div>
      <p class="eyebrow">Framework integrador · Java enterprise</p>
      <h1 id="hero-title">Menos integração.<br><span>Mais produto.</span></h1>
      <p class="hero-lead">O Demoiselle reúne contratos, segurança e capacidades operacionais sobre padrões Jakarta EE para sua equipe concentrar energia no domínio — sem prender o produto a uma pilha proprietária.</p>
      <div class="hero-actions">
        <a class="button button-primary" href="#comecar">Começar agora <span aria-hidden="true">→</span></a>
        <a class="button button-secondary" href="{{ site.github_url }}">Explorar no GitHub {% include icon.html name="external" class="icon-inline" %}</a>
      </div>
      <ul class="technology-list" aria-label="Tecnologias-base">
        <li>Java 21</li>
        <li>Jakarta EE 10</li>
        <li>CDI 4.0</li>
        <li>Maven 3.9+</li>
      </ul>
    </div>

    <div class="hero-visual reveal-target" aria-label="O Demoiselle conecta o domínio do produto aos padrões Jakarta EE e ao runtime escolhido">
      <div class="visual-orbit orbit-one" aria-hidden="true"></div>
      <div class="visual-orbit orbit-two" aria-hidden="true"></div>
      <div class="visual-node product-node">
        <span class="node-kicker">Seu produto</span>
        <strong>Domínio</strong>
        <small>regras que geram valor</small>
      </div>
      <div class="visual-connector connector-in" aria-hidden="true"><span></span></div>
      <div class="framework-node">
        <img class="brand-lockup brand-lockup-inverse" src="{{ '/assets/img/demoiselle-logo-inverse.svg' | relative_url }}" width="234" height="177" alt="Demoiselle Framework" loading="eager" decoding="async">
        <img class="brand-lockup brand-lockup-default" src="{{ '/assets/img/demoiselle-logo.svg' | relative_url }}" width="234" height="177" alt="" loading="eager" decoding="async">
        <span class="node-kicker">Camada integradora</span>
        <div class="node-tags"><span>REST</span><span>CRUD</span><span>Security</span><span>MCP</span></div>
      </div>
      <div class="visual-connector connector-out" aria-hidden="true"><span></span></div>
      <div class="runtime-row">
        <div class="visual-node"><strong>Jakarta EE</strong><small>padrões abertos</small></div>
        <div class="visual-node"><strong>Seu runtime</strong><small>escolha consciente</small></div>
      </div>
    </div>
  </div>

  <div class="shell metrics-strip reveal-target" aria-label="Indicadores do projeto">
    <div><strong>16</strong><span>módulos no reactor</span></div>
    <div><strong>247</strong><span>testes automatizados</span></div>
    <div><strong>6</strong><span>extensões de produção</span></div>
    <div><strong>LGPL</strong><span>software livre</span></div>
  </div>
</section>

<section class="section value-section" id="valor" aria-labelledby="value-title">
  <div class="shell">
    <header class="section-heading reveal-target">
      <p class="eyebrow">Valor para o produto</p>
      <h2 id="value-title">Da complexidade recorrente a uma base previsível</h2>
      <p>O framework não substitui o domínio nem esconde a plataforma. Ele resolve a integração repetitiva e oferece contratos comuns para que decisões importantes sejam explícitas, testáveis e reutilizáveis.</p>
    </header>

    <div class="value-grid">
      <article class="value-card reveal-target">
        <span class="card-number">01</span><span class="card-icon">{% include icon.html name="layers" %}</span>
        <h3>Produtividade sem colcha de retalhos</h3>
        <dl><dt>Problema</dt><dd>Equipes repetem escolhas, configuração e integração em cada serviço.</dd><dt>Solução Demoiselle</dt><dd>BOM único, módulos coesos e convenções CDI sobre Java e Jakarta EE.</dd><dt>Benefício</dt><dd>Menos boilerplate, onboarding mais rápido e versões alinhadas.</dd></dl>
      </article>

      <article class="value-card reveal-target">
        <span class="card-number">02</span><span class="card-icon">{% include icon.html name="contract" %}</span>
        <h3>APIs que clientes entendem</h3>
        <dl><dt>Problema</dt><dd>Erros, paginação e evolução variam entre endpoints e produtos.</dd><dt>Solução Demoiselle</dt><dd>Suporte a Problem Details, Web Linking, OpenAPI e lifecycle HTTP.</dd><dt>Benefício</dt><dd>Contratos mais previsíveis, interoperáveis e fáceis de evoluir.</dd></dl>
      </article>

      <article class="value-card reveal-target">
        <span class="card-number">03</span><span class="card-icon">{% include icon.html name="shield" %}</span>
        <h3>Segurança como contrato</h3>
        <dl><dt>Problema</dt><dd>JWT permissivo, rate limit isolado e proxies mal configurados ampliam riscos.</dd><dt>Solução Demoiselle</dt><dd>Perfis JWT, stores atômicos, headers seguros, HashCash e defaults fail-closed.</dd><dt>Benefício</dt><dd>Políticas consistentes e menor superfície para abuso, replay e spoofing.</dd></dl>
      </article>

      <article class="value-card reveal-target">
        <span class="card-number">04</span><span class="card-icon">{% include icon.html name="loop" %}</span>
        <h3>Consistência em fluxos distribuídos</h3>
        <dl><dt>Problema</dt><dd>Retries duplicam comandos e dual writes perdem eventos após o commit.</dd><dt>Solução Demoiselle</dt><dd>Idempotência, Outbox, deduplicação e paginação por cursor assinado.</dd><dt>Benefício</dt><dd>Operações recuperáveis e integração confiável quando adapters de produção são usados.</dd></dl>
      </article>

      <article class="value-card reveal-target">
        <span class="card-number">05</span><span class="card-icon">{% include icon.html name="pulse" %}</span>
        <h3>Operação observável</h3>
        <dl><dt>Problema</dt><dd>Instrumentação manual acopla o domínio e dificulta diagnosticar incidentes.</dd><dt>Solução Demoiselle</dt><dd>Anotações para métricas e tracing, health checks e integrações opcionais.</dd><dt>Benefício</dt><dd>Sinais operacionais padronizados e menor tempo para localizar falhas.</dd></dl>
      </article>

      <article class="value-card reveal-target">
        <span class="card-number">06</span><span class="card-icon">{% include icon.html name="verified" %}</span>
        <h3>Governança que acompanha o código</h3>
        <dl><dt>Problema</dt><dd>Dependências, artefatos e compatibilidade ficam difíceis de auditar.</dd><dt>Solução Demoiselle</dt><dd>SBOM, proveniência, checksums, inventário, gates e actions pinadas.</dd><dt>Benefício</dt><dd>Releases rastreáveis e decisões de supply chain verificáveis.</dd></dl>
      </article>
    </div>
  </div>
</section>

<section class="section capabilities-section" id="capacidades" aria-labelledby="capabilities-title">
  <div class="shell">
    <header class="section-heading section-heading-split reveal-target">
      <div><p class="eyebrow">Capacidades conectadas</p><h2 id="capabilities-title">Uma base modular para o ciclo completo</h2></div>
      <p>Adote apenas os módulos necessários. O BOM mantém o conjunto compatível e os SPIs permitem trocar infraestrutura sem levar SDKs externos para o núcleo.</p>
    </header>

    <div class="bento-grid">
      <article class="bento-card bento-wide accent-flame reveal-target">
        <div class="bento-icon">{% include icon.html name="database" %}</div><p class="card-overline">CRUD & JPA 3.1</p>
        <h3>Persistência expressiva, sem repetir infraestrutura</h3>
        <p>Specifications compostas, soft delete, auditoria, batch, EntityGraph, filtros tipados, cache e limites contra abuso.</p>
        <ul class="tag-list"><li>Specification</li><li>PageResult</li><li>CriteriaUpdate</li><li>Keyset</li></ul>
      </article>

      <article class="bento-card bento-tall accent-amber reveal-target">
        <div class="bento-icon">{% include icon.html name="shield" %}</div><p class="card-overline">Segurança</p>
        <h3>Identidade, autorização e proteção em camadas</h3>
        <p>JWT com perfis progressivos, seleção segura de <code>kid</code>, autorização declarativa, rate limit, brute force, anti-replay e HashCash.</p>
        <div class="security-layers" aria-label="Camadas de segurança"><span>Headers HTTP</span><span>JWT & roles</span><span>Rate limit</span><span>Replay protection</span></div>
        <small>Perfis avançados são opt-in; configuração inválida falha fechado.</small>
      </article>

      <article class="bento-card accent-rust reveal-target">
        <div class="bento-icon">{% include icon.html name="globe" %}</div><p class="card-overline">REST & OpenAPI</p>
        <h3>Contratos HTTP evolutivos</h3>
        <p>Problem Details opt-in, paginação RFC 8288, cache tipado, documentação agregada e headers de depreciação.</p>
      </article>

      <article class="bento-card accent-gold reveal-target">
        <div class="bento-icon">{% include icon.html name="key" %}</div><p class="card-overline">Configuração & segredos</p>
        <h3>Ambientes sem segredos no código</h3>
        <p>Perfis, defaults tipados e resolução fail-closed por ambiente, propriedade ou arquivo, extensível para vaults corporativos.</p>
      </article>

      <article class="bento-card bento-wide accent-amber reveal-target">
        <div class="bento-icon">{% include icon.html name="loop" %}</div><p class="card-overline">Consistência</p>
        <h3>Primitivas para sistemas que recebem retries</h3>
        <p><code>@Idempotent</code> preserva respostas, Outbox separa commit de publicação e cursores HMAC mantêm paginação estável.</p>
        <a class="text-link" href="{{ '/docs/production-extensions.html' | relative_url }}">Preparar adapters de produção <span aria-hidden="true">→</span></a>
      </article>

      <article class="bento-card accent-flame reveal-target">
        <div class="bento-icon">{% include icon.html name="pulse" %}</div><p class="card-overline">Observabilidade</p>
        <h3>Sinais operacionais declarativos</h3>
        <p><code>@Counted</code>, <code>@Traced</code>, liveness e readiness com adapters opcionais e degradação noop.</p>
      </article>

      <article class="bento-card accent-gold reveal-target">
        <div class="bento-icon">{% include icon.html name="sparkle" %}</div><p class="card-overline">MCP</p>
        <h3>Produtos preparados para clientes de IA</h3>
        <p>Tools, resources e prompts CDI com JSON Schema, SSE ou stdio. Ao ativar segurança HTTP, sessões e POST operam fail-closed.</p>
      </article>

      <article class="bento-card accent-rust reveal-target">
        <div class="bento-icon">{% include icon.html name="package" %}</div><p class="card-overline">Supply chain</p>
        <h3>Build com evidência</h3>
        <p>CycloneDX 1.6, checksums, proveniência, reprodutibilidade normalizada, JaCoCo e inventário dinâmico.</p>
      </article>
    </div>
  </div>
</section>

<section class="section architecture-section" id="arquitetura" aria-labelledby="architecture-title">
  <div class="shell">
    <header class="section-heading reveal-target">
      <p class="eyebrow">Arquitetura aberta</p>
      <h2 id="architecture-title">Integra onde agrega. Abstrai onde protege.</h2>
      <p>O produto continua baseado em APIs conhecidas. Demoiselle organiza preocupações transversais e expõe SPIs nos pontos em que infraestrutura e contexto operacional realmente variam.</p>
    </header>

    <div class="architecture-map reveal-target">
      <div class="architecture-layer domain-layer"><span>Camada 01</span><strong>Seu domínio e suas APIs</strong><p>Entidades, casos de uso, regras e contratos que diferenciam o produto.</p></div>
      <div class="layer-arrow" aria-hidden="true">↓</div>
      <div class="architecture-layer framework-layer"><span>Camada 02</span><strong>Demoiselle Framework</strong><div class="module-cloud"><span>core</span><span>configuration</span><span>crud</span><span>rest</span><span>security</span><span>jwt</span><span>observability</span><span>openapi</span><span>mcp</span><span>script</span></div></div>
      <div class="layer-arrow" aria-hidden="true">↓</div>
      <div class="architecture-bottom">
        <div class="architecture-layer"><span>Camada 03</span><strong>Padrões Jakarta EE</strong><p>CDI, JAX-RS, JPA e APIs MicroProfile opcionais.</p></div>
        <div class="architecture-layer"><span>Extensões</span><strong>Infraestrutura do produto</strong><p>Redis, Vault, JWKS, banco, broker e runtime escolhidos por adapters.</p></div>
      </div>
    </div>

    <aside class="portability-note reveal-target">
      <div><span class="note-icon">{% include icon.html name="info" %}</span><div><strong>Portabilidade sem promessa vazia</strong><p>WildFly 27+, Open Liberty 23.x e Quarkus 3.x são alvos declarados. Cada produto deve certificar em CI o runtime e a configuração que realmente opera.</p></div></div>
      <a class="text-link" href="{{ '/docs/production-extensions.html#4-matriz-executada-de-runtimes' | relative_url }}">Ver estratégia de certificação <span aria-hidden="true">→</span></a>
    </aside>
  </div>
</section>

<section class="section production-section" aria-labelledby="production-title">
  <div class="shell production-panel reveal-target">
    <div class="production-copy">
      <p class="eyebrow">Do framework ao ambiente real</p>
      <h2 id="production-title">Local para começar. Extensível para escalar.</h2>
      <p>Backends locais deixam desenvolvimento e testes autocontidos. Para clusters e releases críticas, o produto conecta estado distribuído, segredos gerenciados, outbox persistente, certificação de runtime, baseline OpenAPI e builds herméticos.</p>
      <a class="button button-primary" href="{{ '/docs/production-extensions.html' | relative_url }}">Conhecer as 6 extensões <span aria-hidden="true">→</span></a>
    </div>
    <ol class="production-list">
      <li><span>01</span>Estado distribuído</li><li><span>02</span>Chaves e segredos</li><li><span>03</span>Outbox e broker</li><li><span>04</span>Matriz de runtimes</li><li><span>05</span>Baseline OpenAPI</li><li><span>06</span>Build hermético</li>
    </ol>
  </div>
</section>

<section class="section start-section" id="comecar" aria-labelledby="start-title">
  <div class="shell start-grid">
    <div class="start-copy reveal-target">
      <p class="eyebrow">Início rápido</p>
      <h2 id="start-title">Uma base alinhada em poucos minutos</h2>
      <p>Importe o BOM, escolha os módulos necessários e mantenha a plataforma Jakarta EE no runtime. A versão atual é um snapshot e requer o repositório Sonatype OSS.</p>
      <ol class="start-steps"><li><span>1</span><div><strong>Use Java 21 e Maven 3.9+</strong><small>O Enforcer verifica o baseline durante o build.</small></div></li><li><span>2</span><div><strong>Importe o BOM</strong><small>Todos os módulos permanecem na mesma versão.</small></div></li><li><span>3</span><div><strong>Adicione por capacidade</strong><small>Comece com core, REST e CRUD; evolua conforme o produto.</small></div></li></ol>
      <a class="text-link" href="{{ '/docs/' | relative_url }}">Abrir guia completo <span aria-hidden="true">→</span></a>
    </div>

    <div class="code-window reveal-target">
      <div class="code-toolbar"><div class="window-dots" aria-hidden="true"><span></span><span></span><span></span></div><span>pom.xml</span><button type="button" class="copy-button js-only" data-copy-target="quickstart-code">Copiar</button></div>
      <pre id="quickstart-code"><code>&lt;dependencyManagement&gt;
  &lt;dependencies&gt;
    &lt;dependency&gt;
      &lt;groupId&gt;org.demoiselle.jee&lt;/groupId&gt;
      &lt;artifactId&gt;demoiselle-parent-bom&lt;/artifactId&gt;
      &lt;version&gt;4.1.0-SNAPSHOT&lt;/version&gt;
      &lt;type&gt;pom&lt;/type&gt;
      &lt;scope&gt;import&lt;/scope&gt;
    &lt;/dependency&gt;
  &lt;/dependencies&gt;
&lt;/dependencyManagement&gt;

&lt;dependency&gt;
  &lt;groupId&gt;org.demoiselle.jee&lt;/groupId&gt;
  &lt;artifactId&gt;demoiselle-rest&lt;/artifactId&gt;
&lt;/dependency&gt;</code></pre>
      <div class="code-footer"><span aria-hidden="true">●</span> BOM mantém versões consistentes</div>
    </div>
  </div>
</section>

<section class="section docs-section" aria-labelledby="docs-title">
  <div class="shell">
    <header class="section-heading reveal-target"><p class="eyebrow">Explore</p><h2 id="docs-title">Documentação para cada decisão</h2><p>Comece pela visão técnica, avalie impactos de migração e avance para os adapters exigidos pelo ambiente do produto.</p></header>
    <div class="docs-card-grid">
      <a class="docs-card reveal-target" href="{{ '/docs/' | relative_url }}"><span class="docs-card-icon">{% include icon.html name="book" %}</span><div><strong>Guia técnico</strong><p>Funcionalidades, configuração e exemplos dos módulos.</p></div><span aria-hidden="true">→</span></a>
      <a class="docs-card reveal-target" href="{{ '/docs/migration-4.1.html' | relative_url }}"><span class="docs-card-icon">{% include icon.html name="swap" %}</span><div><strong>Migração 4.1</strong><p>Compatibilidade, defaults e contratos alterados.</p></div><span aria-hidden="true">→</span></a>
      <a class="docs-card reveal-target" href="{{ '/docs/production-extensions.html' | relative_url }}"><span class="docs-card-icon">{% include icon.html name="server" %}</span><div><strong>Extensões de produção</strong><p>Benefícios e critérios para os seis adapters operacionais.</p></div><span aria-hidden="true">→</span></a>
      <a class="docs-card reveal-target" href="{{ '/docs/roadmap.html' | relative_url }}"><span class="docs-card-icon">{% include icon.html name="map" %}</span><div><strong>Roadmap entregue</strong><p>Auditoria, implementação e invariantes do ciclo.</p></div><span aria-hidden="true">→</span></a>
    </div>
  </div>
</section>

<section class="section ecosystem-section" id="ecossistema" aria-labelledby="ecosystem-title">
  <div class="shell">
    <header class="section-heading section-heading-split reveal-target">
      <div><p class="eyebrow">Ecossistema Demoiselle</p><h2 id="ecosystem-title">Uma história aberta, com produtos para diferentes necessidades</h2></div>
      <p>O novo GitHub Pages dá continuidade ao portal institucional. Ele separa o framework atual dos componentes independentes e preserva caminhos para documentação de gerações anteriores.</p>
    </header>

    <div class="ecosystem-grid">
      <article class="ecosystem-card ecosystem-featured reveal-target">
        <div class="ecosystem-card-top"><span class="ecosystem-symbol">{% include icon.html name="layers" %}</span><span class="status-pill status-current">Ciclo atual</span></div>
        <p class="card-overline">Framework</p>
        <h3>Demoiselle Framework 4</h3>
        <p>Base integradora Java 21 e Jakarta EE 10 deste repositório, com módulos de configuração, CRUD, REST, segurança, observabilidade, OpenAPI e MCP.</p>
        <a class="text-link" href="{{ '/docs/' | relative_url }}">Explorar documentação <span aria-hidden="true">→</span></a>
      </article>

      <article class="ecosystem-card reveal-target">
        <div class="ecosystem-card-top"><span class="ecosystem-symbol">{% include icon.html name="pen" %}</span><span class="status-pill status-active">Projeto independente</span></div>
        <p class="card-overline">Certificação digital</p>
        <h3>Demoiselle Signer</h3>
        <p>Componente para geração e validação de assinaturas digitais e manipulação de certificados nos padrões da ICP-Brasil.</p>
        <a class="text-link" href="https://github.com/demoiselle/signer">Ver projeto Signer {% include icon.html name="external" class="icon-inline" %}</a>
      </article>

      <article class="ecosystem-card reveal-target">
        <div class="ecosystem-card-top"><span class="ecosystem-symbol">{% include icon.html name="beaker" %}</span><span class="status-pill status-legacy">Legado / referência</span></div>
        <p class="card-overline">Testes funcionais</p>
        <h3>dbehave</h3>
        <p>Ferramenta open source de automação funcional baseada em BDD, preservada como referência da trajetória e do ecossistema Demoiselle.</p>
        <a class="text-link" href="https://github.com/demoiselle/behave">Consultar repositório {% include icon.html name="external" class="icon-inline" %}</a>
      </article>
    </div>

    <div class="legacy-links reveal-target">
      <div><p class="eyebrow">Gerações anteriores</p><strong>Precisa manter ou consultar um produto legado?</strong></div>
      <div><a href="https://www.frameworkdemoiselle.gov.br/v3/jee7/index.html">Portal Demoiselle V3 {% include icon.html name="external" class="icon-inline" %}</a><a href="https://demoiselle.gitbooks.io/documentacao-jee/content">Documentação legada {% include icon.html name="external" class="icon-inline" %}</a><a href="https://www.frameworkdemoiselle.gov.br/">Portal institucional {% include icon.html name="external" class="icon-inline" %}</a></div>
    </div>

    <aside class="community-panel reveal-target" id="comunidade">
      <div><span class="community-mark">{% include icon.html name="code" %}</span><div><p class="eyebrow">Comunidade</p><h3>Software livre cresce com colaboração.</h3><p>Discuta melhorias, relate problemas e acompanhe os demais projetos da organização Demoiselle no GitHub.</p></div></div>
      <div class="community-actions"><a class="button button-primary" href="https://github.com/demoiselle">Ver organização</a><a class="button button-secondary" href="{{ site.github_url }}/issues">Abrir uma issue</a></div>
    </aside>
  </div>
</section>

<section class="final-cta">
  <div class="shell final-cta-inner reveal-target">
    {% include brand-mark.html %}
    <p class="eyebrow">Software livre desde 2009</p>
    <h2>Construa sobre padrões.<br>Entregue o que torna seu produto único.</h2>
    <div><a class="button button-primary" href="#comecar">Adicionar ao projeto</a><a class="button button-secondary" href="{{ site.github_url }}/issues">Conversar com a comunidade</a></div>
  </div>
</section>
