import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -jar CompiladorEtapa2.jar <arquivo_fonte>");
            System.exit(1);
            return;
        }

        try (LexerEtapa2 lexer = new LexerEtapa2(args[0])) {
            new ParserEtapa2(lexer).program();
            System.out.println("Analise sintatica concluida com SUCESSO!");
        } catch (IOException | SyntaxException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
