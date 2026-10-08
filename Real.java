public class Real extends Token {
    public final String value;

    public Real(String value) {
        super(Tag.REAL);
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
