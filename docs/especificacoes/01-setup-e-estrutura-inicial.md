# Objetivo

Estabelecer a infraestrutura inicial e a arquitetura base do software de referência didática **TenisShop**, definindo a estrutura de projeto Java Maven no Apache NetBeans 28, o gerenciamento de dependências no `pom.xml`, o modelo físico de banco de dados relacional MySQL e o mecanismo utilitário de conexão JDBC.

# Contexto

A disciplina Informática e Sociedade III demanda a construção e implantação de softwares completos combinando Java, JavaFX e MySQL para solucionar demandas reais de entidades parceiras da região de Venâncio Aires (Escola Monte das Tabocas, Prefeitura Municipal, CTG Chaleira Preta e Secretaria de Educação de Venâncio Aires). Para orientar os estudantes com um modelo arquitetural de alta qualidade e com separação clara de responsabilidades, o TenisShop é desenvolvido de forma evolutiva pelo professor ao longo das aulas.

# Situação Atual

O repositório oficial da disciplina `20262_IS3` foi inicializado via `git init`. O ambiente dos laboratórios de informática do câmpus possui a IDE Apache NetBeans 28, o JDK 21 e o servidor MySQL Server instalados. O projeto Maven `TenisShop` já foi criado no NetBeans, com o `pom.xml` configurado (Group Id `br.edu.ifsul.venancio`, Artifact Id `TenisShop`) e a classe principal `TenisShop.java` gerada automaticamente pelo assistente de criação, no pacote base `br.edu.ifsul.venancio.tenisshop`.

# O que deverá ser desenvolvido

1. Configuração do gerenciador de dependências (`pom.xml`) contemplando o driver JDBC `mysql-connector-j` e os módulos `javafx-controls` e `javafx-fxml`.
2. Criação do banco de dados relacional `tenisshop_db` e da tabela `usuarios`.
3. Implementação da classe de domínio `Usuario.java` no pacote `br.edu.ifsul.venancio.tenisshop.model.domain`.
4. Implementação da classe utilitária de conexão JDBC `ConexaoBanco.java` no pacote `br.edu.ifsul.venancio.tenisshop.model.dao`.
5. Evolução da classe principal `TenisShop.java`, no pacote base `br.edu.ifsul.venancio.tenisshop`, com testes de infraestrutura da aplicação.

# Requisitos Funcionais

- **RF01:** O sistema deve estabelecer e encerrar conexões JDBC seguras com o banco de dados MySQL local.
- **RF02:** O sistema deve mapear os atributos da entidade `Usuario` (`id`, `nome`, `email`, `senha`, `perfil`, `ativo`, `dataCadastro`).
- **RF03:** O sistema deve testar a presença e a funcionalidade da conexão com o banco de dados durante a execução do método `main`.

# Requisitos Não Funcionais

- **RNF01:** A linguagem de programação adotada deve ser Java (JDK 21).
- **RNF02:** O gerenciamento de dependências e compilação deve ser feito via Apache Maven.
- **RNF03:** A codificação de caracteres em todos os arquivos-fonte e na base de dados deve ser obrigatoriamente `UTF-8`.
- **RNF04:** A arquitetura do projeto deve respeitar a separação modular de responsabilidades em pacotes (`model.domain`, `model.dao`), mantendo a classe principal `TenisShop.java` no pacote base.

# Estrutura Esperada

```text
TenisShop/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── br/
                └── edu/
                    └── ifsul/
                        └── venancio/
                            └── tenisshop/
                                ├── TenisShop.java
                                └── model/
                                    ├── dao/
                                    │   └── ConexaoBanco.java
                                    └── domain/
                                        └── Usuario.java
```

# Critérios de Aceitação

- [ ] O projeto Java Maven compila e executa sem erros no NetBeans 28.
- [ ] O `pom.xml` contém as dependências `mysql-connector-j`, `javafx-controls` e `javafx-fxml` corretamente configuradas.
- [ ] O banco de dados `tenisshop_db` e a tabela `usuarios` foram criados no MySQL local.
- [ ] A classe `Usuario.java` implementa todos os atributos definidos (`id`, `nome`, `email`, `senha`, `perfil`, `ativo`, `dataCadastro`), com construtores, getters, setters e `toString()`.
- [ ] A classe `ConexaoBanco.java` estabelece e encerra a conexão JDBC corretamente, reaproveitando a conexão ativa enquanto ela estiver aberta.
- [ ] A execução de `TenisShop.java` confirma, via console, o sucesso (ou a falha, com mensagem de erro clara) do teste de conexão com o banco de dados.
- [ ] Todos os arquivos-fonte e a base de dados utilizam a codificação UTF-8.
- [ ] A estrutura de pacotes segue exatamente a hierarquia documentada, com `TenisShop.java` no pacote base e as camadas `model.domain` e `model.dao`.

# Observações Importantes

- Este documento cobre exclusivamente a etapa de setup e estrutura inicial do TenisShop; as camadas de exceções, utilitários, DAO completo, services, telas gráficas (FXML) e controllers serão tratadas em especificações subsequentes, conforme o cronograma da disciplina.
- A classe principal do projeto é `TenisShop.java`, no pacote base `br.edu.ifsul.venancio.tenisshop`, gerada pelo assistente de criação de projeto do NetBeans; não existe um pacote `app` separado.
- A senha de acesso ao MySQL utilizada nos exemplos é um valor de desenvolvimento local, a ser ajustada conforme a configuração de cada máquina de laboratório.
- A identidade do Git usada nos commits deste projeto deve ser configurada localmente (`git config --local`), evitando alterar o perfil global das máquinas do laboratório.
