// Subclasse que identifica um valor numérico real 
// (ponto flutuante). 

public class Real extends Token {
    public final String value;

    public Real(String value) {
        super(Tag.REAL);
        this.value = value;
    }

    // Guardar os valores como strings evita 
    // o problema de arredondamento.
    @Override
    public String toString() {
        return value;
    }
}
