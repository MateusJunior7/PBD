package EngenhariaReversa;

public class ChaveEstrangeira {
	 private final String nomeTabela;
	    private final String nomeColuna;
	    private final String nomeTabelaEstrangeira;
	    private final String nomeColunaEstrangeira;

	    public ChaveEstrangeira(String nomeTabela, String nomeColuna, String nomeTabelaEstrangeira, String nomeColunaEstrangeira) {
	        this.nomeTabela = nomeTabela;
	        this.nomeColuna = nomeColuna;
	        this.nomeTabelaEstrangeira = nomeTabelaEstrangeira;
	        this.nomeColunaEstrangeira = nomeColunaEstrangeira;
	    }

	    public String getNomeTabela() {
	        return nomeTabela;
	    }

	    public String getNomeColuna() {
	        return nomeColuna;
	    }

	    public String getNomeTabelaEstrangeira() {
	        return nomeTabelaEstrangeira;
	    }

	    public String getNomeColunaEstrangeira() {
	        return nomeColunaEstrangeira;
	    }
}
