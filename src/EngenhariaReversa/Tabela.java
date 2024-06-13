package EngenhariaReversa;

import java.util.ArrayList;
import java.util.List;

public class Tabela {
	private final String nomeSchema;
    private final String nomeTabela;
    private final List<Coluna> colunas;

    public Tabela(String nomeSchema, String nomeTabela) {
        this.nomeSchema = nomeSchema;
        this.nomeTabela = nomeTabela;
        this.colunas = new ArrayList<>();
    }

    public String getNomeSchema() {
        return nomeSchema;
    }

    public String getNomeTabela() {
        return nomeTabela;
    }

    public List<Coluna> getColunas() {
        return colunas;
    }

    public void addColuna(Coluna coluna) {
        colunas.add(coluna);
    }

}
