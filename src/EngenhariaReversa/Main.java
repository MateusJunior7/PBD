package EngenhariaReversa;


import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            List<Tabela> tabelas = obterTabelas(statement);
            List<ChaveEstrangeira> chavesEstrangeiras = obterChavesEstrangeiras(statement);

            marcarChavesPrimarias(statement, tabelas);
            marcarChavesEstrangeiras(tabelas, chavesEstrangeiras);
            gerarArquivoDot(tabelas, chavesEstrangeiras, "diagrama.dot");

            for (Tabela tabela : tabelas) {
                System.out.println("Tabela: " + tabela.getNomeTabela());
                for (Coluna coluna : tabela.getColunas()) {
                    System.out.print("    Coluna: " + coluna.getNomeColuna() + " - " + coluna.getTipoDado());
                    if (coluna.isPrimaryKey()) {
                        System.out.print(" [PK]");
                    }
                    if (coluna.isForeignKey()) {
                        System.out.print(" [FK]");
                    }
                    System.out.println();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Tabela> obterTabelas(Statement statement) throws Exception {
        List<Tabela> tabelas = new ArrayList<>();
        String query = "SELECT table_schema, table_name, column_name, data_type " +
                "FROM information_schema.columns " +
                "WHERE table_schema NOT IN ('pg_catalog', 'information_schema')";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            String schema = resultSet.getString("table_schema");
            String nomeTabela = resultSet.getString("table_name");
            String nomeColuna = resultSet.getString("column_name");
            String tipoDado = resultSet.getString("data_type");

            Tabela tabela = tabelas.stream()
                    .filter(t -> t.getNomeTabela().equals(nomeTabela))
                    .findFirst()
                    .orElseGet(() -> {
                        Tabela t = new Tabela(schema, nomeTabela);
                        tabelas.add(t);
                        return t;
                    });
            tabela.addColuna(new Coluna(nomeColuna, tipoDado));
        }
        return tabelas;
    }

    private static List<ChaveEstrangeira> obterChavesEstrangeiras(Statement statement) throws Exception {
        List<ChaveEstrangeira> chavesEstrangeiras = new ArrayList<>();
        String query = "SELECT tc.table_schema, tc.table_name, kcu.column_name, " +
                "ccu.table_schema AS foreign_table_schema, ccu.table_name AS foreign_table_name, " +
                "ccu.column_name AS foreign_column_name " +
                "FROM information_schema.table_constraints AS tc " +
                "JOIN information_schema.key_column_usage AS kcu " +
                "ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema " +
                "JOIN information_schema.constraint_column_usage AS ccu " +
                "ON ccu.constraint_name = tc.constraint_name " +
                "WHERE tc.constraint_type = 'FOREIGN KEY'";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            String nomeTabela = resultSet.getString("table_name");
            String nomeColuna = resultSet.getString("column_name");
            String nomeTabelaEstrangeira = resultSet.getString("foreign_table_name");
            String nomeColunaEstrangeira = resultSet.getString("foreign_column_name");
            chavesEstrangeiras.add(new ChaveEstrangeira(nomeTabela, nomeColuna, nomeTabelaEstrangeira, nomeColunaEstrangeira));
        }
        return chavesEstrangeiras;
    }

    private static void marcarChavesPrimarias(Statement statement, List<Tabela> tabelas) throws Exception {
        String query = "SELECT kcu.table_schema, kcu.table_name, kcu.column_name " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu " +
                "ON tc.constraint_name = kcu.constraint_name " +
                "WHERE tc.constraint_type = 'PRIMARY KEY'";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            String nomeTabela = resultSet.getString("table_name");
            String nomeColuna = resultSet.getString("column_name");

            tabelas.stream()
                    .filter(t -> t.getNomeTabela().equals(nomeTabela))
                    .flatMap(t -> t.getColunas().stream())
                    .filter(c -> c.getNomeColuna().equals(nomeColuna))
                    .forEach(c -> c.setPrimaryKey(true));
        }
    }

    private static void marcarChavesEstrangeiras(List<Tabela> tabelas, List<ChaveEstrangeira> chavesEstrangeiras) {
        for (ChaveEstrangeira fk : chavesEstrangeiras) {
            tabelas.stream()
                    .filter(t -> t.getNomeTabela().equals(fk.getNomeTabela()))
                    .flatMap(t -> t.getColunas().stream())
                    .filter(c -> c.getNomeColuna().equals(fk.getNomeColuna()))
                    .forEach(c -> c.setForeignKey(true));
        }
    }

    private static void gerarArquivoDot(List<Tabela> tabelas, List<ChaveEstrangeira> chavesEstrangeiras, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("digraph ERD {\n");
            writer.write("    rankdir=LR;\n");
            writer.write("    node [shape=record];\n");

            // Escrever tabelas e suas colunas
            for (Tabela tabela : tabelas) {
                StringBuilder sb = new StringBuilder();
                sb.append(tabela.getNomeTabela()).append(" [label=\"{");
                sb.append(tabela.getNomeTabela()).append("|");

                for (Coluna coluna : tabela.getColunas()) {
                    sb.append(coluna.getNomeColuna()).append(" : ").append(coluna.getTipoDado());
                    if (coluna.isPrimaryKey()) sb.append(" (PK)");
                    if (coluna.isForeignKey()) sb.append(" (FK)");
                    sb.append("\\l");
                }
                sb.append("}\"];\n");
                writer.write(sb.toString());
            }

            // Escrever chaves estrangeiras
            for (ChaveEstrangeira fk : chavesEstrangeiras) {
                writer.write("    " + fk.getNomeTabela() + " -> " + fk.getNomeTabelaEstrangeira() +
                        " [label=\"" + fk.getNomeColuna() + " -> " + fk.getNomeColunaEstrangeira() + "\"];\n");
            }

            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}