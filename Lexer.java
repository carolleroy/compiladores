import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1; // contador de linhas
    private char ch = ' ';      // caractere lido do arquivo
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
        reserve(new Word("read", Tag.READ));
        reserve(new Word("write", Tag.WRITE));
        reserve(new Word("program", Tag.PRG));
        reserve(new Word("begin", Tag.BEG));
        reserve(new Word("end", Tag.END));
        reserve(new Word("int", Tag.INT));
        reserve(new Word("char", Tag.CHAR));
        reserve(new Word("float", Tag.FLOAT));
    }

    /* Lê o próximo caractere do arquivo */
    private void readch() throws IOException {
        ch = (char) arquivo.read();
    }

    /* Lê o próximo caractere do arquivo e verifica se é igual a c */
    private boolean readch(char c) throws IOException {
        readch();
        if (ch != c) return false;
        ch = ' ';
        return true;
    }

    // Métodos que aceitam só ASCII, como a gramática
    // define: letter ::= [A-Za-z], digit ::= [0-9]
    private boolean identificaLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean identificaDigito(char c) {
        return c >= '0' && c <= '9';
    }

    public Token scan() throws IOException {
        // Desconsidera delimitadores, espaços e comentários na entrada
        for (;; readch()) {
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\b') {
                continue;
            } else if (ch == '\n') {
                line++; // conta linhas
            
            // Tratamento de comentários multilinha {* ... *}
            } else if (ch == '{') {
                int startLine = line;
                readch();
                if (ch == '*') {
                    boolean fimComentario = false;
                    while (!fimComentario) { 
                        readch();
                        if (ch == '\n') line++;
                        if (ch == '*') {
                            readch();
                            if (ch == '}') {
                                fimComentario = true;
                            }
                        }

                        // file.read() devolve um int: o código do caractere 
                        // lido, ou -1 quando o arquivo acabou. Ou seja, se achou um -1
                        // significa que chegou ao fim do arquivo sem achar o 
                        if (ch == (char) -1) {
                            System.err.println("Erro lexico (linha " + startLine + "): comentario nao fechado.");
                            break;
                        }
                    }
                } else {
                    // Se não for comentário, devolve a chave '{' como token se necessário
                    return new Token('{');
                }
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
            int valor = 0;
            do {

                //Charactter.digit converte o número para a base decimal
                //Ai, vai multiplicando por 10 e somando até chegar no número
                //que queremos.
                valor = 10 * valor + Character.digit(ch, 10);

                readch();
            } while (identificaDigito(ch));
            return new Num(valor);
        }

        // Identificadores e Palavras Reservadas
        if (identificaLetra(ch) || ch == '_') {
            StringBuffer sb = new StringBuffer();
            do {
                sb.append(ch);
                readch();
            } while (identificaLetra(ch) || identificaDigito(ch) || ch == '_');
            
            String s = sb.toString();
            Word w = words.get(s);
            if (w != null) return w; // palavra já existe na HashTable (Reservada)
            
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        // Caracteres não especificados (pontuação isolada, etc.)
        Token t = new Token(ch);
        ch = ' ';
        return t;
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