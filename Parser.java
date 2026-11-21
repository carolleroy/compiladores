import java.io.IOException;

// Analisador Sintático Descendente
// O algoritmo deve ser recursivo descendente, uma 
// das formas mais simples de se implementar um analisador.

public class Parser {
    private final Lexer lexer;
    private Token token;


    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        advance(); // Lê o primeiro token da entrada.
    }

    // Funções semelhantes às que estudamos em sala:

    // Avança para o próximo token.
    private void advance() throws IOException {
        token = lexer.scan();
    }

    // Interrompe a análise e exibe uma mensagem de erro 
    // e encerra a execução do programa.Não há recuperação 
    // de erro.
    private void error(String message) {
        throw new SyntaxException("Erro Sintatico (linha " + 
        lexer.getTokenLine() + "): " + message + 
        " Encontrado: '" + token + "'.");
        //System.exit(1);
    }

    // Verifica se o token atual é do tipo esperado. 
    // Se sim, avança para o próximo token. Se não, dá erro.
    private void eat(int t) throws IOException {
        if (token.tag == t) {
            advance();
        } else {
            error("Token inesperado " + token + ". Token que era esperado: " + nome(t) + ".");
        }
    }

    // Nome legível de um token, usado nas mensagens de erro.
    // Ai não fica aparecendo os números, menos intuitivos.
    private String nome(int t) {
        switch (t) {
            case Lexer.EOF:  return "fim de arquivo";
            case Tag.ID:     return "identificador";
            case Tag.PRG:    return "'program'";
            case Tag.BEG:    return "'begin'";
            case Tag.END:    return "'end'";
            case Tag.THEN:   return "'then'";
            case Tag.DO:     return "'do'";
            case Tag.UNTIL:  return "'until'";
            case Tag.READ:   return "'read'";
            case Tag.WRITE:  return "'write'";
            case Tag.REPEAT: return "'repeat'";
            case Tag.WHILE:  return "'while'";
            case Tag.IF:     return "'if'";
            default:         return "'" + (char) t + "'"; // caracteres simples: ; , ( ) = .
        }
    }


    // Cada regra na gramática terá sua própria função.
    // As regras originais das gramáticas que precisarem de modificações 
    // por questões de ambiguidade no parser, terão funções auxiliares, que
    // terão nomes de letras gregas. Isso foi uma escolha nossa mesmo, pois
    // dessa forma, os nossos esquemas não ficariam confusos.
    // Para saber mais, leia o relatório final, o PDF, lá você encontra imagens
    // e melhores explicações.


    // program ::= program identifier begin [decl-list] stmt-list end "."
    public void program() throws IOException {
        eat(Tag.PRG);   // Identifica a palavra reservada, program
        eat(Tag.ID);    // Identifica um id
        eat(Tag.BEG);  // Identifica apalavra reservada, begin
        
        // CORREÇÃO 1: aqui estamos arrumando um problema de ambiguidade,
        // especificamente, na hora de fazer x = "alguma coisa" (um tipo,
        // um inteiro...) é importante que nós certifiquemos que o parser
        // terá certeza de qual é o próximo token lido, pra sabermos pra onde
        // ele vai ir.

        alpha();
    }

    // Fim do programa: end "." e, logo depois, o fim do arquivo (S' -> S $ dos slides).
    private void fimPrograma() throws IOException {
        eat(Tag.END);
        eat('.');
        eat(Lexer.EOF);
        // Acaba aqui, reconheceu.
    }

    // alpha ::= identifier beta | stmt_sem_assign epsilon end "."
    private void alpha() throws IOException {

        if (token.tag == Tag.IF || token.tag == Tag.WHILE 
            || token.tag == Tag.REPEAT || token.tag == Tag.READ
            || token.tag == Tag.WRITE) {
            stmt_sem_assign();
            epsilon();
            fimPrograma();
            // Acaba aqui, reconheceu.
        } 
        else if (token.tag == Tag.ID) {
            eat(Tag.ID);
            beta();
        }
        else {
             error("Comando invalido ou inesperado.");
        }

    }

    // stmt_sem_assign ::= if-stmt | while-stmt | 
    // repeat-stmt | read-stmt | write-stmt
     private void stmt_sem_assign() throws IOException {
        switch (token.tag) {
            case Tag.IF:
                ifStmt();
                break;
            case Tag.WHILE:
                whileStmt();
                break;
            case Tag.REPEAT:
                repeatStmt();
                break;
            case Tag.READ:
                readStmt();
                break;
            case Tag.WRITE:
                writeStmt();
                break;
            default:
                error("Comando invalido ou inesperado.");
        }

    }

    // beta ::= "," identifier delta "=" type ";" alpha
    //        | "=" gamma
    private void beta() throws IOException {
        if(token.tag == ','){
            eat(',');
            eat(Tag.ID);
            delta();
            eat('=');
            type();
            eat(';');
            alpha();
        }
        else if (token.tag == '='){
            eat('=');
            gamma();
        }
        else {
            error("Esperado ',' ou '='.");
        }
    }

    // gamma ::= type ";" alpha
    //         | simple-expr epsilon end "."
    private void gamma() throws IOException {
        if(token.tag == Tag.INT || token.tag == Tag.FLOAT 
        || token.tag == Tag.CHAR) {
            type();
            eat(';');
            alpha();
        }
        else {
            simpleExpr();
            epsilon();
            fimPrograma();
            // Acaba aqui, reconheceu.
        }
    }

    // delta ::= "," identifier delta | vazio
    private void delta() throws IOException {
        if(token.tag == ','){
            eat(',');
            eat(Tag.ID);
            delta();
        }
    }

    // epsilon ::= ";" stmt epsilon | vazio
    private void epsilon() throws IOException {
         if(token.tag == ';'){
            eat(';');
            stmt();
            epsilon();
         }
    }
    /*
    // CORREÇÃO 2: As próximas 3 funções poderão ser comentadas, pois devido
    // às modificações deitas para que a gramática se encaixasse no parser, 
    // essas regras foram encaixadas em outras funções.

    // decl-list ::= decl ";" { decl ";" }
    private void declList() throws IOException {
        decl();
        eat(';');
        while (token.tag == Tag.ID) {
            decl();
            eat(';');
        }
    }

    // decl ::= ident-list "=" type
    private void decl() throws IOException {
        identList();
        eat('=');
        type();
    }

    // ident-list ::= identifier { "," identifier }
    private void identList() throws IOException {
        eat(Tag.ID);
        while (token.tag == ',') {
            advance();
            eat(Tag.ID);
        }
    }*/

    // type ::= int | float | char
    private void type() throws IOException {
        if (token.tag == Tag.INT || token.tag == Tag.FLOAT 
        || token.tag == Tag.CHAR) {
            advance();
        } else {
            error("Tipo invalido em declaracao de variavel.");
        }
    }

    // stmt-list ::= stmt epsilon
    // Também modificada por causa das alterações de cima
    private void stmtList() throws IOException {
        stmt();
        epsilon();
    }

    // stmt ::= assign-stmt | if-stmt | while-stmt | 
    // repeat-stmt | read-stmt | write-stmt
    private void stmt() throws IOException {
        switch (token.tag) {
            case Tag.ID:
                assignStmt();
                break;
            case Tag.IF:
                ifStmt();
                break;
            case Tag.WHILE:
                whileStmt();
                break;
            case Tag.REPEAT:
                repeatStmt();
                break;
            case Tag.READ:
                readStmt();
                break;
            case Tag.WRITE:
                writeStmt();
                break;
            default:
                error("Comando invalido ou inesperado.");
        }
    }

    // assign-stmt ::= identifier "=" simple_expr
    private void assignStmt() throws IOException {
        eat(Tag.ID);
        eat('=');
        simpleExpr();
    }

    // CORREÇÃO 3: também consertando a ambiguidade no início
    //dessa regra.

    // if-stmt ::= if condition then stmt-list zeta end
    private void ifStmt() throws IOException {
        eat(Tag.IF);
        condition();
        eat(Tag.THEN);
        stmtList();
        zeta();
        eat(Tag.END);
    }

    // zeta ::= else stmt-list | vazio
    private void zeta() throws IOException {
        if (token.tag == Tag.ELSE) {
            advance();
            stmtList();
        } // senao deriva para 'vazio'
    }

    // condition ::= expression
    private void condition() throws IOException {
        expression();
    }

    // repeat-stmt ::= repeat stmt-list stmt-suffix
    private void repeatStmt() throws IOException {
        eat(Tag.REPEAT);
        stmtList();
        suffixStmt();
    }

    // stmt-suffix ::= until condition
    private void suffixStmt() throws IOException {
        eat(Tag.UNTIL);
        condition();
    }

    // while-stmt ::= stmt-prefix stmt-list end
    private void whileStmt() throws IOException {
        prefixStmt();
        stmtList();
        eat(Tag.END);
    }

    // stmt-prefix ::= while condition do
    private void prefixStmt() throws IOException {
        eat(Tag.WHILE);
        condition();
        eat(Tag.DO);
    }

    // read-stmt ::= read "(" identifier ")"
    private void readStmt() throws IOException {
        eat(Tag.READ);
        eat('(');
        eat(Tag.ID);
        eat(')');
    }

    // write-stmt ::= write "(" writable ")"
    private void writeStmt() throws IOException {
        eat(Tag.WRITE);
        eat('(');
        writable();
        eat(')');
    }

    // writable ::= simple-expr | literal
    private void writable() throws IOException {
        if (token.tag == Tag.LITERAL) {
            advance();
        } else {
            simpleExpr();
        }
    }

    // CORREÇÃO 4:
    // expression ::= simple-expr eta
    private void expression() throws IOException {
        simpleExpr();
        eta();
    }

    // eta ::= relop simple-expr | vazio
    private void eta() throws IOException {
        if (isRelop(token.tag)) {
            advance(); // consome relop
            simpleExpr();
        } // senao deriva X -> vazio
    }

    // CORREÇÃO 5:
    // simple-expr ::= term theta
    private void simpleExpr() throws IOException {
        term();
        theta();
    }
    
    // theta ::= addop term R | vazio
    private void theta() throws IOException {
        if (token.tag == '+' || token.tag == '-' || 
        token.tag == Tag.OR) {
            advance(); // consome addop
            term();
            theta();
        } // senao deriva R -> vazio
    }

    // CORREÇÃO 6: eliminação da recursão à esquerda.
    // term ::= fator-a iota
    private void term() throws IOException {
        fatorA();
        iota();
    }

    private void iota() throws IOException {
        if (token.tag == '*' || token.tag == '/' || 
        token.tag == '%' || token.tag == Tag.AND) {
            advance(); // consome mulop, recursao a direita
            fatorA();
            iota();    // recursao a direita
        } // senao deriva S -> vazio
    }

    // fator-a ::= fator | ! fator | "-" fator
    private void fatorA() throws IOException {
        if (token.tag == '!') {
            advance();
            fator();
        } else if (token.tag == '-') {
            advance();
            fator();
        } else {
            fator();
        }
    }

    // fator ::= identifier | constant | "(" expression ")"
    private void fator() throws IOException {
        if (token.tag == Tag.ID || token.tag == Tag.NUM || 
        token.tag == Tag.REAL || token.tag == Tag.CHAR) {
            advance();
        } else if (token.tag == '(') {
            advance();
            expression();
            eat(')');
        } else {
            error("Fator invalido na expressao.");
        }
    }

    private boolean isRelop(int tokenTag) {
        return tokenTag == Tag.EQ || tokenTag == '>' || tokenTag == Tag.GE ||
               tokenTag == '<' || tokenTag == Tag.LE || tokenTag == Tag.NE;
    }
}
