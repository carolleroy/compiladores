import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1; // contador de linhas
    private int ch = ' ';       // caractere lido do arquivo
    private FileReader arquivo;

    //tabela de símbolos
    private Hashtable<String, Word> words = new Hashtable<>();

    // Método para inserir palavras reservadas na HashTable
    private void reserve(Word w) {
        words.put(w.getLexema(), w); // lexema é a chave para entrada na HashTable
    }

    // Método construtor
    public Lexer(String nomeArquivo) throws FileNotFoundException {
        try {
            arquivo = new FileReader(nomeArquivo);
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
            throw e;
        }

        // Insere palavras reservadas na HashTable conforme o material de aula
        reserve(new Word("if", Tag.IF));
        reserve(new Word("then", Tag.THEN));
        reserve(new Word("else", Tag.ELSE));
        reserve(new Word("while", Tag.WHILE));
        reserve(new Word("do", Tag.DO));
        reserve(new Word("repeat", Tag.REPEAT));
        reserve(new Word("until", Tag.UNTIL));
        reserve(new Word("in", Tag.IN));
        reserve(new Word("out", Tag.OUT));
        reserve(new Word("program", Tag.PRG));
        reserve(new Word("begin", Tag.BEG));
        reserve(new Word("end", Tag.END));
        reserve(new Word("int", Tag.INT));
        reserve(new Word("char", Tag.CHAR));
        reserve(new Word("float", Tag.FLOAT));
    }

    /* Lê o próximo caractere do arquivo */
    private void readch() throws IOException {
        ch = arquivo.read();
    }

    /* Lê o próximo caractere do arquivo e verifica se é igual a c */
    private boolean readch(int c) throws IOException {
        readch();
        if (ch != c) return false;
        ch = ' ';
        return true;
    }

    // Métodos que aceitam só ASCII, como a gramática
    // define: letter ::= [A-Za-z], digit ::= [0-9]
    private boolean identificaLetra(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean identificaDigito(int c) {
        return c >= '0' && c <= '9';
    }

    private void erro(String mensagem) throws IOException {
        throw new IOException("Erro lexico (linha " + line + "): " + mensagem);
    }

    public Token scan() throws IOException {
        // Desconsidera delimitadores, espaços e comentários na entrada
        for (;; readch()) {
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\b') {
                continue;
            } else if (ch == '\n') {
                line++; // conta linhas
            } else if (ch == -1) {
                return new Token(-1);

            // Tratamento de comentários multilinha { ... }
            } else if (ch == '{') {
                int startLine = line;
                readch();
                while (ch != '}') {
                    if (ch == -1) {
                        throw new IOException("Erro lexico (linha " + startLine + "): comentario nao fechado.");
                    }
                    if (ch == '\n') line++;
                    readch();
                }
            } else if (ch == '%') {
                do {
                    readch();
                } while (ch != '\n' && ch != -1);
                if (ch == '\n') line++;
            } else {
                break;
            }
        }

        switch (ch) {
            // Operadores Compostos e Simples
            case '&':
                if (readch('&')) return Word.and;
                else return new Token('&');
            case '|':
                if (readch('|')) return Word.or;
                else return new Token('|');
            case '=':
                if (readch('=')) return Word.eq;
                else return new Token('=');
            case '!':
                if (readch('=')) return Word.ne;
                else return new Token('!');
            case '<':
                if (readch('=')) return Word.le;
                else return new Token('<');
            case '>':
                if (readch('=')) return Word.ge;
                else return new Token('>');
        }


        // Números Inteiros
        if (identificaDigito(ch)) {
            StringBuffer sb = new StringBuffer();
            do {
                sb.append((char) ch);
                readch();
            } while (identificaDigito(ch));

            if (ch == '.') {
                sb.append((char) ch);
                readch();

                if (!identificaDigito(ch)) {
                    erro("constante real mal formada.");
                }

                do {
                    sb.append((char) ch);
                    readch();
                } while (identificaDigito(ch));

                return new Real(sb.toString());
            }

            return new Num(Integer.parseInt(sb.toString()));
        }

        // Literais entre aspas duplas
        if (ch == '"') {
            StringBuffer sb = new StringBuffer();
            readch();

            while (ch != '"') {
                if (ch == '\n' || ch == -1) {
                    erro("literal nao fechado.");
                }
                sb.append((char) ch);
                readch();
            }

            ch = ' ';
            return new Literal(sb.toString());
        }

        // Constantes de caractere entre aspas simples
        if (ch == '\'') {
            readch();
            if (ch == '\n' || ch == -1 || ch == '\'') {
                erro("constante de caractere mal formada.");
            }

            char value = (char) ch;
            readch();
            if (ch != '\'') {
                erro("constante de caractere deve possuir apenas um caractere.");
            }

            ch = ' ';
            return new CharConst(value);
        }

        // Identificadores e Palavras Reservadas
        if (identificaLetra(ch) || ch == '_') {
            StringBuffer sb = new StringBuffer();
            do {
                sb.append((char) ch);
                readch();
            } while (identificaLetra(ch) || identificaDigito(ch) || ch == '_');
            
            String s = sb.toString().toLowerCase();
            Word w = words.get(s);
            if (w != null) return w; // palavra já existe na HashTable (Reservada)
            
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        if (";,:(()+-*/".indexOf(ch) >= 0) {
            Token t = new Token(ch);
            ch = ' ';
            return t;
        }

        erro("caractere invalido '" + (char) ch + "'.");
        return new Token(-1);
    }

    public void printSymbolTable() {
        System.out.println("\n========================================");
        System.out.println("           TABELA DE SIMBOLOS           ");
        System.out.println("========================================");

        Enumeration<String> keys = words.keys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Word w = words.get(key);
            System.out.println("Lexema: " + key + " \t| Tag: " + w.tag);
        }

        System.out.println("========================================");
    }
}
