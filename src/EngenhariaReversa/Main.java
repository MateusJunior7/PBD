package EngenhariaReversa;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5433/Produto";
        String user = "postgres";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            List<Tabela> tabelas = obterTabelas(statement);
            List<ChaveEstrangeira> chavesEstrangeiras = obterChavesEstrangeiras(statement);

            gerarArquivoDot(tabelas, chavesEstrangeiras, "diagrama.dot");

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
                    sb.append(coluna.getNomeColuna()).append(" : ").append(coluna.getTipoDado()).append("\\l");
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