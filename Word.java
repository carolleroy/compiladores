public class Word extends Token {
    private String lexema = "";

    public static final Word 
        and = new Word("&&", Tag.AND),
        or  = new Word("||", Tag.OR),
        eq  = new Word("==", Tag.EQ),
        ne  = new Word("!=", Tag.NE),
        le  = new Word("<=", Tag.LE),
        ge  = new Word(">=", Tag.GE);

    public Word(String s, int tag) {
        super(tag);
        lexema = s;
    }

    public String getLexema() {
        return lexema;
    }

    @Override
    public String toString() {
        return lexema;
    }
}