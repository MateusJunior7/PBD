package EngenhariaReversa;

public class Coluna {
	   private final String nomeColuna;
	    private final String tipoDado;

	    public Coluna(String nomeColuna, String tipoDado) {
	        this.nomeColuna = nomeColuna;
	        this.tipoDado = tipoDado;
	    }

	    public String getNomeColuna() {
	        return nomeColuna;
	    }

	    public String getTipoDado() {
	        return tipoDado;
	    }
}
