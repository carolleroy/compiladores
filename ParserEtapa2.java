import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/** Analisador descendente recursivo, sem geradores e sem recuperacao de erros. */
public final class ParserEtapa2 {
    private final LexerEtapa2 lexer;
    private final Deque<LocatedToken> pending = new ArrayDeque<>();

    private static final class LocatedToken {
        final Token token;
        final int line;
        LocatedToken(Token token, int line) { this.token = token; this.line = line; }
    }

    public ParserEtapa2(LexerEtapa2 lexer) { this.lexer = lexer; }

    private LocatedToken peek(int index) throws IOException {
        while (pending.size() <= index) {
            Token token = lexer.scan();
            pending.addLast(new LocatedToken(token, lexer.getTokenLine()));
        }
        int i = 0;
        for (LocatedToken token : pending) if (i++ == index) return token;
        throw new AssertionError("Lookahead indisponivel");
    }

    private int tag() throws IOException { return peek(0).token.tag; }
    private void move() throws IOException { peek(0); pending.removeFirst(); }

    private void error(String expected) throws IOException {
        LocatedToken current = peek(0);
        throw new SyntaxException("Erro sintatico (linha " + current.line + "): esperado "
            + expected + "; encontrado '" + current.token + "'.");
    }

    private void match(int expected) throws IOException {
        if (tag() != expected) error(name(expected));
        move();
    }

    private static String name(int tag) {
        switch (tag) {
            case LexerEtapa2.EOF: return "fim de arquivo";
            case Tag.ID: return "identificador";
            case Tag.PRG: return "'program'";
            case Tag.BEG: return "'begin'";
            case Tag.END: return "'end'";
            case Tag.THEN: return "'then'";
            case Tag.DO: return "'do'";
            case Tag.UNTIL: return "'until'";
            default: return "'" + (char) tag + "'";
        }
    }

    private static boolean isType(int tag) {
        return tag == Tag.INT || tag == Tag.FLOAT || tag == Tag.CHAR;
    }

    // Declaracoes e atribuicoes comecam por ID '='. A decisao exige ate 3 tokens.
    // ID ',' so pode iniciar declaracao; ID '=' type tambem.
    private boolean startsDeclaration() throws IOException {
        return tag() == Tag.ID && (peek(1).token.tag == ','
            || peek(1).token.tag == '=' && isType(peek(2).token.tag));
    }

    // program ::= program identifier begin [decl-list] stmt-list end '.' EOF
    public void program() throws IOException {
        match(Tag.PRG);
        match(Tag.ID);
        match(Tag.BEG);
        while (startsDeclaration()) {
            identList();
            match('=');
            if (!isType(tag())) error("tipo int, float ou char");
            move();
            match(';');
        }
        stmtList();
        match(Tag.END);
        match('.');
        match(LexerEtapa2.EOF);
    }

    private void identList() throws IOException {
        match(Tag.ID);
        while (tag() == ',') { move(); match(Tag.ID); }
    }

    // Lista obrigatoriamente nao vazia; ';' separa comandos, nao os termina.
    private void stmtList() throws IOException {
        stmt();
        while (tag() == ';') { move(); stmt(); }
    }

    private void stmt() throws IOException {
        switch (tag()) {
            case Tag.ID:
                move(); match('='); simpleExpr(); break;
            case Tag.IF:
                move(); expression(); match(Tag.THEN); stmtList();
                if (tag() == Tag.ELSE) { move(); stmtList(); }
                match(Tag.END); break;
            case Tag.WHILE:
                move(); expression(); match(Tag.DO); stmtList(); match(Tag.END); break;
            case Tag.REPEAT:
                move(); stmtList(); match(Tag.UNTIL); expression(); break;
            case Tag.IN:
                move(); match('('); match(Tag.ID); match(')'); break;
            case Tag.OUT:
                move(); match('(');
                if (tag() == Tag.LITERAL) move(); else simpleExpr();
                match(')'); break;
            default:
                error("comando (atribuicao, if, while, repeat, read ou write)");
        }
    }

    // expression ::= simple-expr [relop simple-expr]
    private void expression() throws IOException {
        simpleExpr();
        if (isRelop(tag())) { move(); simpleExpr(); }
    }

    // A recursao a esquerda foi substituida por repeticao.
    private void simpleExpr() throws IOException {
        term();
        while (tag() == '+' || tag() == '-' || tag() == Tag.OR) { move(); term(); }
    }

    private void term() throws IOException {
        factorA();
        while (tag() == '*' || tag() == '/' || tag() == '%' || tag() == Tag.AND) {
            move(); factorA();
        }
    }

    private void factorA() throws IOException {
        if (tag() == '!' || tag() == '-') move();
        factor();
    }

    private void factor() throws IOException {
        switch (tag()) {
            case Tag.ID: case Tag.NUM: case Tag.REAL: case Tag.CHAR_CONST:
                move(); break;
            case '(':
                move(); expression(); match(')'); break;
            default:
                error("identificador, constante ou expressao entre parenteses");
        }
    }

    private static boolean isRelop(int tag) {
        return tag == Tag.EQ || tag == Tag.NE || tag == Tag.GE || tag == Tag.LE || tag == '>' || tag == '<';
    }
}
