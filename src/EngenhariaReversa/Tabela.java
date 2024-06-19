package EngenhariaReversa;

import java.util.ArrayList;
import java.util.List;

public class Tabela {
    private String schema;
    private String nomeTabela;
    private List<Coluna> colunas;

    public Tabela(String schema, String nomeTabela) {
        this.schema = schema;
        this.nomeTabela = nomeTabela;
        this.colunas = new ArrayList<>();
    }

    public String getNomeTabela() {
        return nomeTabela;
    }

    public List<Coluna> getColunas() {
        return colunas;
    }

    public void addColuna(Coluna coluna) {
        this.colunas.add(coluna);
    }
}

