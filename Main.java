package compiladores;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java compiladores.Main <arquivo_fonte>");
            return;
        }

        try {
            Lexer lexer = new Lexer(args[0]);
            Parser parser = new Parser(lexer);
            parser.program(); // Inicia a analise sintatica
        } catch (IOException e) {
            System.err.println("Erro na leitura do arquivo: " + e.getMessage());
        }
    }
}