package br.edu.ifsul.venancio.tenisshop.model.dao;

import br.edu.ifsul.venancio.tenisshop.model.domain.Perfil;
import br.edu.ifsul.venancio.tenisshop.model.util.SenhaUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Aplica automaticamente, toda vez que a aplicação inicia, o schema de
 * banco de dados esperado pelo sistema: cria tabelas que ainda não
 * existem e adiciona colunas que ainda faltam, sem nunca apagar ou
 * sobrescrever dados já existentes. Cada passo é conferido antes de ser
 * executado (idempotência), então rodar isto de novo num banco que já
 * está em dia não tem efeito nenhum.
 *
 * Os scripts em TenisShop/sql/V1..V7 documentam o mesmo schema de forma
 * legível e servem de referência/plano B manual; esta classe é quem de
 * fato aplica tudo sozinha, sem depender de alguém colar SQL no MySQL
 * Workbench. É, em miniatura, a mesma ideia por trás de ferramentas de
 * migração como Flyway ou Liquibase.
 *
 * @author Geovane Griesang
 */
public class SchemaInitializer {

    /**
     * Garante que o schema do banco está atualizado e, se o banco estiver
     * vazio, semeia o primeiro usuário administrador. Deve ser chamado uma
     * vez, no início da aplicação, antes de qualquer tela ser exibida.
     * @throws SQLException caso ocorra falha ao consultar ou alterar o schema
     */
    public static void inicializar() throws SQLException {
        Connection conexao = ConexaoBanco.getConexao();

        criarTabelaUsuariosSeNaoExistir(conexao);
        adicionarColunaDeveTrocarSenhaSeNaoExistir(conexao);
        adicionarColunaVersaoSeNaoExistir(conexao);
        criarTabelaConfiguracaoSistemaSeNaoExistir(conexao);
        criarTabelaTokensRecuperacaoSeNaoExistir(conexao);
        criarTabelaAuditoriaSeNaoExistir(conexao);
        ampliarColunaDetalheSeNecessario(conexao);
        semearUsuarioAdminSeVazio(conexao);
    }

    private static boolean colunaExiste(Connection conexao, String tabela, String coluna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, tabela);
            stmt.setString(2, coluna);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private static void criarTabelaUsuariosSeNaoExistir(Connection conexao) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "nome VARCHAR(100) NOT NULL,"
                + "email VARCHAR(100) NOT NULL UNIQUE,"
                + "senha VARCHAR(255) NOT NULL,"
                + "perfil VARCHAR(20) NOT NULL DEFAULT 'OPERADOR',"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void adicionarColunaDeveTrocarSenhaSeNaoExistir(Connection conexao) throws SQLException {
        if (colunaExiste(conexao, "usuarios", "deve_trocar_senha")) {
            return;
        }
        String sql = "ALTER TABLE usuarios ADD COLUMN deve_trocar_senha BOOLEAN NOT NULL DEFAULT FALSE";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void adicionarColunaVersaoSeNaoExistir(Connection conexao) throws SQLException {
        if (colunaExiste(conexao, "usuarios", "versao")) {
            return;
        }
        String sql = "ALTER TABLE usuarios ADD COLUMN versao INT NOT NULL DEFAULT 1";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void criarTabelaConfiguracaoSistemaSeNaoExistir(Connection conexao) throws SQLException {
        String sqlTabela = "CREATE TABLE IF NOT EXISTS configuracao_sistema ("
                + "id TINYINT NOT NULL PRIMARY KEY DEFAULT 1,"
                + "permitir_autocadastro BOOLEAN NOT NULL DEFAULT FALSE,"
                + "CONSTRAINT chk_configuracao_singleton CHECK (id = 1)"
                + ")";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sqlTabela);
        }

        String sqlSemente = "INSERT IGNORE INTO configuracao_sistema (id, permitir_autocadastro) VALUES (1, FALSE)";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sqlSemente);
        }
    }

    private static void criarTabelaTokensRecuperacaoSeNaoExistir(Connection conexao) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS tokens_recuperacao_senha ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "usuario_id INT NOT NULL,"
                + "token_hash CHAR(64) NOT NULL,"
                + "data_expiracao DATETIME NOT NULL,"
                + "usado BOOLEAN NOT NULL DEFAULT FALSE,"
                + "data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT fk_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),"
                + "INDEX idx_token_hash (token_hash)"
                + ")";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void criarTabelaAuditoriaSeNaoExistir(Connection conexao) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS auditoria ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "usuario_id INT NULL,"
                + "acao VARCHAR(40) NOT NULL,"
                + "entidade VARCHAR(40) NULL,"
                + "entidade_id INT NULL,"
                + "detalhe VARCHAR(500) NULL,"
                + "data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),"
                + "INDEX idx_auditoria_data_hora (data_hora)"
                + ")";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Amplia auditoria.detalhe para VARCHAR(500) se ainda estiver no
     * tamanho antigo, VARCHAR(255): o histórico de campo a campo (Aula
     * 06) precisa de mais espaço que a frase curta genérica de antes.
     * Diferente de adicionarColunaVersaoSeNaoExistir, aqui a checagem não
     * é "a coluna existe?" (ela sempre existiu), e sim "a coluna já está
     * do tamanho certo?", outra pergunta comum ao evoluir um schema aos
     * poucos, e outra forma de consultar INFORMATION_SCHEMA para
     * responder isso sem risco de rodar o ALTER à toa toda vez.
     */
    private static void ampliarColunaDetalheSeNecessario(Connection conexao) throws SQLException {
        String sql = "SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'auditoria' AND COLUMN_NAME = 'detalhe'";

        int tamanhoAtual;
        try (Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (!rs.next()) {
                return; // tabela auditoria ainda não existia antes desta chamada; já nasce no tamanho certo
            }
            tamanhoAtual = rs.getInt(1);
        }

        if (tamanhoAtual >= 500) {
            return;
        }

        String sqlAlterar = "ALTER TABLE auditoria MODIFY COLUMN detalhe VARCHAR(500) NULL";
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sqlAlterar);
        }
    }

    private static void semearUsuarioAdminSeVazio(Connection conexao) throws SQLException {
        String sqlContagem = "SELECT COUNT(*) FROM usuarios";
        int totalUsuarios;
        try (Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(sqlContagem)) {
            rs.next();
            totalUsuarios = rs.getInt(1);
        }

        if (totalUsuarios > 0) {
            return;
        }

        // O hash é calculado agora, na hora de semear, nunca colado pronto
        // num script SQL. Como o BCrypt gera um salt novo a cada chamada,
        // um hash fixo salvo em texto ficaria "desatualizado" e não bateria
        // mais com uma nova geração; calculando em runtime, a única coisa
        // que precisa ficar documentada é a senha em si (IFSul_15anos).
        String hashSenhaAdmin = SenhaUtil.gerarHash("IFSul_15anos");

        String sqlInserir = "INSERT INTO usuarios (nome, email, senha, perfil, ativo, deve_trocar_senha) "
                + "VALUES (?, ?, ?, ?, TRUE, TRUE)";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlInserir)) {
            stmt.setString(1, "Administrador do Sistema");
            stmt.setString(2, "admin@tenisshop.com");
            stmt.setString(3, hashSenhaAdmin);
            stmt.setString(4, Perfil.ADMINISTRADOR.name());
            stmt.executeUpdate();
        }

        System.out.println("[INFO] Banco de dados vazio: usuário administrador semente criado "
                + "(admin@tenisshop.com / IFSul_15anos, com troca de senha obrigatória no primeiro login).");
    }
}
