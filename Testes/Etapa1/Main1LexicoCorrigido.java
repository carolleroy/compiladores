public class Main1LexicoCorrigido {
    public static void main(String[] args) throws Exception {
        try (Lexer lexer = new Lexer(args[0])) {
            // pede tokens ao lexer até o fim do arquivo, sem fazer análise sintática
            while (lexer.scan().tag != Lexer.EOF) { }
            System.out.println("Nenhum erro lexico.");
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }
}