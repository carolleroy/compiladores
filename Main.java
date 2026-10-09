import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Main <arquivo_fonte>");
            return;
        }

        try {
            Lexer.line = 1;
            Lexer lexer = new Lexer(args[0]);
            Token token;

            

            System.out.println("-----------------------------------------");
            System.out.println("          SEQUENCIA DE TOKENS           ");
            System.out.println("-----------------------------------------");

            // Executa o analisador até o fim do arquivo (o FileReader retorna -1 / char 65535 ou similar ao acabar)
            do {
                token = lexer.scan();
                if (token.tag != -1 && token.tag != 0) {
                    System.out.println("Linha " + Lexer.line + " | Token Tag: " + token.tag + " | Lexema/Valor: [" + token + "]");
                }
            } while (token.tag != -1 && token.tag != 0);

            // Exibe a Tabela de Símbolos preenchida ao final (exigência da Etapa 1)
            lexer.printSymbolTable();

        } catch (IOException e) {
            System.err.println("Erro na leitura do arquivo: " + e.getMessage());
        }
    }
}
