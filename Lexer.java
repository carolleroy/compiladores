import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

// Classe closeable, diz que o objeto dessa classe segura 
// recursos que devem ser fechados após o uso.
public class Lexer implements Closeable{

    public static final int EOF = -1; // tag usada para indicar fim de arquivo.

    private int line = 1; // contador de linhas.
    private int tokenLine = 1;  // linha onde começou o último token retornado por scan().
    private int ch = ' ';       // caractere que está sendo analisado agora, como lookahead.
    private int anterior = ' '; // caractere lido antes de ch (para tratar o \r\n do Windows).
    private Reader arquivo;

    // words = tabela de símbolos
    private Hashtable<String, Word> words = new Hashtable<>();

    // Método para inserir palavras reservadas na HashTable.
    private void reserve(Word w) {
        words.put(w.getLexema(), w); // lexema é a chave para entrada na HashTable.
    }

    // Método construtor
    public Lexer(String nomeArquivo) throws IOException {
        try {
            // Lê o arquivo como UTF-8 (mesma codificação em qualquer sistema operacional)
            arquivo = new InputStreamReader(new FileInputStream(nomeArquivo), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
            throw e;
        }

        // Insere palavras reservadas na HashTable conforme o material de aula.
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

        // Alguns editores (ex.: Bloco de Notas) colocam um caractere invisível (BOM)
        // no início de arquivos UTF-8. Se ele existir, é ignorado.
        readch();
        if (ch == 0xFEFF) ch = ' ';
    }

    /* Lê o próximo caractere do arquivo */
    private void readch() throws IOException {
        anterior = ch;
        ch = arquivo.read();
    }

    /* Conta uma linha nova. Aceita \n (Linux), \r\n (Windows) e \r (Mac antigo):
       o \n que vem logo depois de um \r não é contado de novo. */
    private void contaLinha() {
        if (ch == '\r' || (ch == '\n' && anterior != '\r')) line++;
    }

    /* Linha do último token lido. O parser usa nas mensagens de erro. */
    public int getTokenLine() {
        return tokenLine;
    }

    /* Fecha o arquivo-fonte. */
    @Override
    public void close() throws IOException {
        arquivo.close();
    }

    /* Lê o próximo caractere do arquivo e verifica se é igual a c (caractere esperado) 
    ùtil para operadores compostos, como &&, ||, ==, !=, <=, >=, depois de um < ele checa
    se vem um =. a fim de formar um <=.
    */
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
        throw new IOException("Erro lexico (linha " + tokenLine + "): " + mensagem);
    }

    public Token scan() throws IOException {
        // Desconsidera delimitadores, espaços e comentários na entrada,
        // basicamente, pula o que não é token.

        for (;; readch()) {
            if (ch == ' ' || ch == '\t' || ch == '\b') {
                continue;
            } else if (ch == '\n' || ch == '\r') {
                contaLinha(); // conta linhas
            } else if (ch == EOF) {
                tokenLine = line;
                return new Token(EOF);

            // Tratamento de comentários multilinha { ... }
            } else if (ch == '{') {
                int startLine = line;
                readch();
                if (ch == '*') { // Confirma que abriu com {*
                    readch();
                    boolean fechou = false;
                    while (ch != -1) {
                        if (ch == '*') {
                            readch();
                            if (ch == '}') {
                                fechou = true;
                                ch = ' ';
                                break;
                            }
                        } else {
                            if (ch == '\n' || ch == '\r') contaLinha();
                            readch();
                        }
                    }
                    if (!fechou) throw new IOException("Erro lexico (linha " + startLine + "): comentario nao fechado.");
                } else {
                    erro("caractere invalido '{'.");
                }
            } else {
                break;
            }
        }
        

        // Aqui começa um token: guarda a linha dele.
        tokenLine = line;

        switch (ch) {
            // Tratamento de operadores Compostos e Simples
            case '&':
                if (readch('&')) return Word.and;
                // '&' sozinho não existe na gramática
                erro("operador incompleto '&' (esperado '&&').");
                break;
            case '|':
                if (readch('|')) return Word.or;
                // '|' sozinho não existe na gramática
                erro("operador incompleto '|' (esperado '||').");
                break;
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


        // Reconhecedor de número inteiros e reais.
        if (identificaDigito(ch)) {
            StringBuffer sb = new StringBuffer();
            do {
                sb.append((char) ch);
                readch();
            } while (identificaDigito(ch));

            if (ch == '.') {
                sb.append((char) ch);
                readch();

                // Se identifica algo que não é um número 
                // no meio do processo, dá erro.
                if (!identificaDigito(ch)) {
                    erro("constante real mal formada.");
                }

                do {
                    sb.append((char) ch);
                    readch();
                } while (identificaDigito(ch));

                // Verifica se há uma letra colada logo após o número real (ex: 1.5c)
                if (identificaLetra(ch) || ch == '_') {
                    erro("identificador mal formado (nao pode iniciar com numero).");
                }

                return new Real(sb.toString());
            }

            // Verifica se há uma letra colada logo após o número inteiro (ex: 1c)
            if (identificaLetra(ch) || ch == '_') {
                erro("identificador mal formado (nao pode iniciar com numero).");
            }

            try {
                return new Num(Integer.parseInt(sb.toString()));
            } catch (NumberFormatException e) {
                erro("constante inteira muito grande.");
            }
        }

        // Literais entre aspas duplas
        if (ch == '"') {
            StringBuffer sb = new StringBuffer();
            readch();

            while (ch != '"') {
                if (ch == '\n' || ch == '\r' || ch == EOF) {
                    erro("literal nao fechado.");
                }

                // a gramática só aceita caracteres ASCII
                if (ch > 127) {
                    erro("literal deve conter apenas caracteres ASCII.");
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
            if (ch == '\n' || ch == '\r' || ch == EOF) {
                erro("constante de caractere mal formada.");
            }

            if (ch > 127) {
                    erro("constante de caractere deve conter apenas caracteres ASCII.");
            }

            char value = (char) ch;
            readch();
            if (ch != '\'') {
                erro("constante de caractere deve possuir apenas um caractere.");
            }

            ch = ' ';
            return new CharConst(value);
        }

        // Identifica identificadores (cria uma word, com tag ID) e Palavras Reservadas
        if (identificaLetra(ch) || ch == '_') {
            StringBuffer sb = new StringBuffer();
            do {
                sb.append((char) ch);
                readch();
            } while (identificaLetra(ch) || identificaDigito(ch) || ch == '_');
            
            String s = sb.toString();
            Word w = words.get(s);
            if (w != null) return w; // palavra já existe na HashTable (Reservada)
            
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        // Identifica os símbolos simples, viram um token com o valor 
        // do próprio caractere na tabela ASCII.
        if (";,:.(()+-*/%".indexOf(ch) >= 0) { //incluindo o ponto final 
            Token t = new Token(ch);
            ch = ' ';
            return t;
        }

        // Caracter não presente na linguagem.
        erro("caractere invalido '" + (char) ch + "'.");
        return new Token(EOF);
    }

    /*public void printSymbolTable() {
        System.out.println("\n-----------------------------------------");
        System.out.println("           TABELA DE SIMBOLOS           ");
        System.out.println("-----------------------------------------");

        Enumeration<String> keys = words.keys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Word w = words.get(key);
            System.out.println("Lexema: " + key + " \t| Tag: " + w.tag);
        }

        System.out.println("-----------------------------------------");
    }*/
}