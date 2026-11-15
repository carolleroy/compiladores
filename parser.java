package compiladores;

import java.io.IOException;

public class Parser {
    private final Lexer lexer;
    private Token lookahead;

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        move();
    }

    private void move() throws IOException {
        lookahead = lexer.scan();
    }

    private void error(String message) {
        System.err.println("Erro Sintatico [Linha " + Lexer.line + "]: " + message);
        System.exit(1);
    }

    private void match(int t) throws IOException {
        if (lookahead.tag == t) {
            move();
        } else {
            error("Token inesperado '" + lookahead + "'. Esperado código: " + t);
        }
    }

    // program ::= program identifier begin [decl-list] stmt-list end "."
    public void program() throws IOException {
        match(Tag.PROGRAM);
        match(Tag.ID);
        match(Tag.BEGIN);
        
        // Verifica se há declarações (se o lookahead for um identificador)
        if (lookahead.tag == Tag.ID) {
            declList();
        }
        
        stmtList();
        match(Tag.END);
        match('.');
        System.out.println("Compilacao sintatica concluida com SUCESSO!");
    }

    // decl-list ::= decl ";" { decl ";" }
    private void declList() throws IOException {
        decl();
        match(';');
        while (lookahead.tag == Tag.ID) {
            decl();
            match(';');
        }
    }

    // decl ::= ident-list "=" type
    private void decl() throws IOException {
        identList();
        match('=');
        type();
    }

    // ident-list ::= identifier { "," identifier }
    private void identList() throws IOException {
        match(Tag.ID);
        while (lookahead.tag == ',') {
            move();
            match(Tag.ID);
        }
    }

    // type ::= int | float | char
    private void type() throws IOException {
        if (lookahead.tag == Tag.INT || lookahead.tag == Tag.FLOAT || lookahead.tag == Tag.CHAR_TYPE) {
            move();
        } else {
            error("Tipo invalido em declaracao de variavel.");
        }
    }

    // stmt-list ::= stmt { ";" stmt }
    private void stmtList() throws IOException {
        stmt();
        while (lookahead.tag == ';') {
            move();
            stmt();
        }
    }

    // stmt ::= assign-stmt | if-stmt | while-stmt | repeat-stmt | read-stmt | write-stmt
    private void stmt() throws IOException {
        switch (lookahead.tag) {
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
        match(Tag.ID);
        match('=');
        simpleExpr();
    }

    // if-stmt ::= if condition then stmt-list Y end
    // Y ::= else stmt-list | vazio
    private void ifStmt() throws IOException {
        match(Tag.IF);
        condition();
        match(Tag.THEN);
        stmtList();
        
        // Regra Y
        if (lookahead.tag == Tag.ELSE) {
            move();
            stmtList();
        } // senao deriva para 'vazio'
        
        match(Tag.END);
    }

    // condition ::= expression
    private void condition() throws IOException {
        expression();
    }

    // repeat-stmt ::= repeat stmt-list stmt-suffix
    // stmt-suffix ::= until condition
    private void repeatStmt() throws IOException {
        match(Tag.REPEAT);
        stmtList();
        match(Tag.UNTIL);
        condition();
    }

    // while-stmt ::= stmt-prefix stmt-list end
    // stmt-prefix ::= while condition do
    private void whileStmt() throws IOException {
        match(Tag.WHILE);
        condition();
        match(Tag.DO);
        stmtList();
        match(Tag.END);
    }

    // read-stmt ::= read "(" identifier ")"
    private void readStmt() throws IOException {
        match(Tag.READ);
        match('(');
        match(Tag.ID);
        match(')');
    }

    // write-stmt ::= write "(" writable ")"
    private void writeStmt() throws IOException {
        match(Tag.WRITE);
        match('(');
        writable();
        match(')');
    }

    // writable ::= simple-expr | literal
    private void writable() throws IOException {
        if (lookahead.tag == Tag.LITERAL) {
            move();
        } else {
            simpleExpr();
        }
    }

    // expression ::= simple-expr X
    // X ::= relop simple-expr | vazio
    private void expression() throws IOException {
        simpleExpr();
        if (isRelop(lookahead.tag)) {
            move(); // consome relop
            simpleExpr();
        } // senao deriva X -> vazio
    }

    // simple-expr ::= term R
    // R ::= addop term R | vazio
    private void simpleExpr() throws IOException {
        term();
        R();
    }

    private void R() throws IOException {
        if (lookahead.tag == '+' || lookahead.tag == '-') {
            move(); // consome addop
            term();
            R(); // recursao a direita
        } // senao deriva R -> vazio
    }

    // term ::= factor-a S
    // S ::= mulop factor-a S | vazio
    private void term() throws IOException {
        factorA();
        S();
    }

    private void S() throws IOException {
        if (lookahead.tag == '*' || lookahead.tag == '/' || lookahead.tag == '%' || lookahead.tag == Tag.AND) {
            move(); // consome mulop
            factorA();
            S(); // recursao a direita
        } // senao deriva S -> vazio
    }

    // factor-a ::= factor | ! factor | "-" factor
    private void factorA() throws IOException {
        if (lookahead.tag == '!') {
            move();
            factor();
        } else if (lookahead.tag == '-') {
            move();
            factor();
        } else {
            factor();
        }
    }

    // factor ::= identifier | constant | "(" expression ")"
    private void factor() throws IOException {
        if (lookahead.tag == Tag.ID || lookahead.tag == Tag.NUM || lookahead.tag == Tag.REAL || lookahead.tag == Tag.CHAR) {
            move();
        } else if (lookahead.tag == '(') {
            move();
            expression();
            match(')');
        } else {
            error("Fator invalido na expressao.");
        }
    }

    private boolean isRelop(int tag) {
        return tag == Tag.EQ || tag == Tag.GT || tag == Tag.GE || 
               tag == Tag.LT || tag == Tag.LE || tag == Tag.NE;
    }
}