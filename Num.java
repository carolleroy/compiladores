// Subclasse que identifica um valor numérico inteiro
// Bem útil para a etapa 3.

public class Num extends Token {
    public final int value;

    public Num(int value) {
        super(Tag.NUM);
        this.value = value;
    }

    @Override
    public String toString() {
        return "" + value;
    }
}