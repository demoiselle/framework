Demoiselle 4
-----------
[![CI](https://github.com/demoiselle/framework/actions/workflows/ci.yml/badge.svg)](https://github.com/demoiselle/framework/actions/workflows/ci.yml) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.demoiselle.jee/demoiselle-core/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/org.demoiselle.jee/demoiselle-core/badge.svg?style=flat-square)

=============

O framework Demoiselle implementa o conceito de framework integrador.
Seu objetivo é facilitar a construção de aplicações minimizando tempo
dedicado à escolha e integração de frameworks especialistas, o que
resulta no aumento da produtividade e garante a manutenibilidade dos sistemas.

Disponibiliza mecanismos reusáveis voltados as funcionalidades mais
comuns de uma aplicação (arquitetura, segurança, transação, mensagem,
configuração, tratamento de exceções, etc).

## Versão 4.0.0

A versão 4 do Demoiselle Framework traz as seguintes mudanças principais:

- **Jakarta EE 10**: Migração completa do namespace `javax.*` para `jakarta.*`
- **Java 17**: Versão mínima do Java atualizada para 17 (LTS)
- **CDI 4.0**: Atualização para Jakarta Contexts and Dependency Injection 4.0
- **JUnit 5**: Migração completa dos testes para JUnit Jupiter
- **OpenAPI 3.0**: Substituição do Swagger 1.x por MicroProfile OpenAPI
- **GitHub Actions**: Pipeline de CI/CD migrado do Travis CI para GitHub Actions
- **Remoção do WildFly Swarm**: Framework agnóstico de runtime (compatível com WildFly 27+, Quarkus, Open Liberty)
- **Remoção do DeltaSpike**: Substituído por implementação própria baseada em CDI 4.0

O nome Demoiselle é uma homenagem à série de aeroplanos construídos
por Santos Dummont entre 1907 e 1909. Também conhecido como Libellule,
as Demoiselles foram os melhores, menores e mais baratos aviões da sua
época. Como sua intenção era popularizar a aviação com fabricação em larga
escala, o inventor disponibilizou os planos em revistas técnicas para
qualquer pessoa que se interessasse.

O framework Demoiselle usa a mesma filosofia do "Pai da Aviação",
tendo sido disponibilizado como software livre em abril de 2009, sob a
licença livre LGPL version 3. Mais informações no [portal](http://demoiselle.io).


Links úteis
-----------

* [Portal](http://demoiselle.io): Central de acesso as informações do Demoiselle
* [Documentação](https://demoiselle.gitbooks.io/documentacao-jee/content): Aprenda sobre o Demoiselle seguindo os vários módulos
* [Fórum/Tracker](https://github.com/demoiselle/framework/issues): Fóruns de discussão e Submissão/acompanhamento de Bugs, Improvements e New Features
* [Lista de discussão](https://lists.sourceforge.net/lists/listinfo/demoiselle-users): Comunicação e troca de experiências entre os usuários do projeto.


Repositório Maven
-----------

    <repository>
        <id>central.repository</id>
        <url>http://repo1.maven.org/maven2</url>
    </repository>


Contribuindo
------------

1. Faça o seu fork.
2. Crie o seu branch (ramo) - (`git checkout -b meu_framework`)
3. Commit seu código (`git commit -am "Explicando o motivo/objetivo"`)
4. Agora execute o Push para o branch (`git push origin meu_framework`)
5. Dúvidas, problemas ou sugestões? Crie uma issue no [GitHub][1] com o link para o seu branch


[1]: https://github.com/demoiselle/framework/issues
