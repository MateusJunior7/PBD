package EngenhariaReversa;


public class Coluna {
    private String nomeColuna;
    private String tipoDado;
    private boolean isPrimaryKey;
    private boolean isForeignKey;

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

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        isForeignKey = foreignKey;
    }
}



