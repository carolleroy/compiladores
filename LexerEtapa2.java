import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Leitor usado exclusivamente na etapa 2; preserva o Lexer entregue na etapa 1. */
public final class LexerEtapa2 implements Closeable {
    public static final int EOF = -1;
    private final BufferedReader input;
    private final Map<String, Integer> keywords = new HashMap<>();
    private int ch;
    private int line = 1;
    private int tokenLine = 1;

    public LexerEtapa2(String path) throws IOException {
        input = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);
        String[] names = {"program", "begin", "end", "int", "float", "char", "if",
            "then", "else", "while", "do", "repeat", "until", "read", "write"};
        int[] tags = {Tag.PRG, Tag.BEG, Tag.END, Tag.INT, Tag.FLOAT, Tag.CHAR, Tag.IF,
            Tag.THEN, Tag.ELSE, Tag.WHILE, Tag.DO, Tag.REPEAT, Tag.UNTIL, Tag.IN, Tag.OUT};
        for (int i = 0; i < names.length; i++) keywords.put(names[i], tags[i]);
        try {
            ch = input.read();
            if (ch == 0xFEFF) advance(); // BOM de arquivos UTF-8
        } catch (IOException e) {
            input.close();
            throw e;
        }
    }

    public int getTokenLine() { return tokenLine; }

    private void advance() throws IOException {
        if (ch == '\r') {
            line++;
            ch = input.read();
            if (ch == '\n') ch = input.read();
        } else {
            if (ch == '\n') line++;
            ch = input.read();
        }
    }

    private static boolean letter(int c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    private static boolean digit(int c) { return c >= '0' && c <= '9'; }

    private IOException error(String message) {
        return new IOException("Erro lexico (linha " + tokenLine + "): " + message);
    }

    public Token scan() throws IOException {
        for (;;) {
            while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f') advance();
            tokenLine = line;
            if (ch != '{') break;
            advance();
            if (ch != '*') throw error("esperado '*' depois de '{'.");
            advance();
            boolean closed = false;
            while (ch != EOF) {
                if (ch == '*') {
                    advance();
                    if (ch == '}') {
                        advance();
                        closed = true;
                        break;
                    }
                } else advance();
            }
            if (!closed) throw error("comentario nao fechado.");
            // O caractere seguinte ja foi lido: nao avancar outra vez.
        }
        if (ch == EOF) return new Token(EOF);
        if (letter(ch) || ch == '_') {
            StringBuilder text = new StringBuilder();
            do { text.append((char) ch); advance(); }
            while (letter(ch) || digit(ch) || ch == '_');
            String word = text.toString();
            return new Word(word, keywords.containsKey(word) ? keywords.get(word) : Tag.ID);
        }
        if (digit(ch)) {
            StringBuilder text = new StringBuilder();
            do { text.append((char) ch); advance(); } while (digit(ch));
            int tag = Tag.NUM;
            if (ch == '.') {
                tag = Tag.REAL;
                text.append('.');
                advance();
                if (!digit(ch)) throw error("constante real mal formada.");
                do { text.append((char) ch); advance(); } while (digit(ch));
            }
            if (letter(ch) || ch == '_') throw error("identificador nao pode iniciar com numero.");
            // A etapa sintatica precisa do lexema e da tag, sem limitar o inteiro a 32 bits.
            return new Word(text.toString(), tag);
        }
        if (ch == '"') {
            StringBuilder text = new StringBuilder();
            advance();
            while (ch != '"') {
                if (ch == EOF || ch == '\n' || ch == '\r') throw error("literal nao fechado.");
                if (ch > 127) throw error("literal deve conter apenas caracteres ASCII.");
                text.append((char) ch);
                advance();
            }
            advance();
            return new Literal(text.toString());
        }
        if (ch == '\'') {
            advance();
            if (ch == EOF || ch > 127) throw error("constante de caractere deve conter um caractere ASCII.");
            char value = (char) ch;
            advance();
            if (ch != '\'') throw error("constante de caractere deve possuir apenas um caractere.");
            advance();
            return new CharConst(value);
        }
        int first = ch;
        advance();
        switch (first) {
            case '&':
            case '|':
                if (ch != first) throw error("operador incompleto '" + (char) first + "'.");
                advance();
                return new Word(first == '&' ? "&&" : "||", first == '&' ? Tag.AND : Tag.OR);
            case '=': case '!': case '<': case '>':
                if (ch == '=') {
                    advance();
                    int tag = first == '=' ? Tag.EQ : first == '!' ? Tag.NE : first == '<' ? Tag.LE : Tag.GE;
                    return new Word("" + (char) first + '=', tag);
                }
                return new Token(first);
            default:
                if (";,:.()+-*/%".indexOf(first) >= 0) return new Token(first);
                throw error("caractere invalido '" + (char) first + "'.");
        }
    }

    @Override
    public void close() throws IOException { input.close(); }
}
