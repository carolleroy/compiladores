import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

/** Testes de regressao sem dependencias externas. Executar com TestarEtapa2.ps1. */
public class TesteParserEtapa2 {
    private static int count;

    private static void check(String name, String source, String expected) throws Exception {
        Path path = Files.createTempFile("compiladores-etapa2-", ".txt");
        try {
            Files.write(path, source.getBytes(StandardCharsets.UTF_8));
            String failure = null;
            try (LexerEtapa2 lexer = new LexerEtapa2(path.toString())) {
                new ParserEtapa2(lexer).program();
            } catch (IOException | SyntaxException e) { failure = e.getMessage(); }
            if (expected == null ? failure != null : failure == null || !failure.contains(expected)) {
                throw new AssertionError(name + ": esperado " + expected + "; obtido " + failure);
            }
            count++;
        } finally { Files.deleteIfExists(path); }
    }

    private static void ok(String name, String body) throws Exception {
        check(name, "program p begin " + body + " end.", null);
    }

    private static void bad(String name, String body, String error) throws Exception {
        check(name, "program p begin " + body + " end.", error);
    }

    public static void main(String[] args) throws Exception {
        ok("sem declaracoes e atribuicao inicial", "x = 1");
        ok("declaracao e atribuicao", "x = int; x = 1");
        ok("varias declaracoes", "a,b = int; c = float; d = char; a = 1; c = 2.5; d = 'S'");
        ok("identificadores", "_ = int; _1,aB9,A = float; _ = 0");
        ok("read", "read(x)");
        ok("write expressao", "write(1 + 2 * 3)");
        ok("write literal vazio", "write(\"\")");
        ok("write literal", "write(\"Ola!\")");
        ok("char", "write('S')");
        ok("char espaco", "write(' ')");
        ok("char aspas simples ASCII", "write(''')");
        ok("char aspas duplas", "write('\"')");
        ok("inteiro longo sem overflow", "write(9999999999999999999999999999999999999)");
        ok("real", "write(0001.500)");
        ok("operadores aritmeticos", "a = -1 + 2 - 3 * 4 / 5 % 6");
        ok("logicos", "a = !b && c || d");
        ok("unarios parentetizados", "a = -(-1); b = !(!a)");
        for (String op : new String[]{"==", "!=", ">", ">=", "<", "<="}) {
            ok("relacional " + op, "if a " + op + " b then write(a) end");
        }
        ok("if else", "if (a > b) then a = 1 else a = 2 end");
        ok("if sem else", "if a then a = 1 end");
        ok("if aninhado", "if a then if b then a = 1 else a = 2 end else a = 3 end");
        ok("while", "while a < 10 do a = a + 1 end");
        ok("repeat", "repeat a = a - 1; write(a) until a == 0");
        ok("aninhamento", "repeat while a do if b then read(a) else write(a) end end until a");
        ok("declaracao opcional com read", "read(a); a = 2");
        ok("declaracao apos atribuicao rejeitada pelo parser", "a = 1; b = 2");
        ok("relacoes combinadas com parenteses", "if (a > b) && (c < d) || e then write(1) end");
        ok("comentario adjacente", "{*ok*}write(1){*fim*}");
        ok("comentarios consecutivos", "{**}{***}{*a\nb*}write(1)");
        check("comentario apos ponto", "program p begin write(1) end.{*fim*}", null);
        check("BOM", "\uFEFFprogram p begin write(1) end.", null);
        check("case sensitive identificadores", "program P begin A,a = int; A = a end.", null);
        bad("bloco vazio", "", "Erro sintatico");
        bad("ponto e virgula final", "write(1);", "encontrado 'end'");
        bad("dois separadores", "write(1);; write(2)", "encontrado ';'");
        bad("sem separador", "write(1) write(2)", "esperado 'end'");
        bad("sem comando depois de declaracao", "a = int;", "comando");
        bad("declaracao tardia", "a = 1; b = int; write(b)", "encontrado 'int'");
        bad("declaracao sem separador", "a = int a = 1", "esperado ';'");
        bad("ident-list incompleta", "a, = int; a = 1", "identificador");
        bad("tipo como constante", "write(char)", "encontrado 'char'");
        bad("palavra reservada como id", "int = 1", "comando");
        bad("read constante", "read(1)", "identificador");
        bad("read varios argumentos", "read(a,b)", "esperado ')'" );
        bad("write sem parenteses", "write 1", "esperado '('" );
        bad("write vazio", "write()", "expressao");
        bad("write varios argumentos", "write(a,b)", "esperado ')'" );
        bad("literal em atribuicao", "a = \"abc\"", "expressao");
        bad("relacao em atribuicao sem parenteses", "a = 1 > 2", "esperado 'end'");
        bad("relacao encadeada", "if a < b < c then write(1) end", "esperado 'then'");
        bad("if sem then", "if a write(1) end", "esperado 'then'");
        bad("if vazio", "if a then end", "comando");
        bad("else vazio", "if a then write(1) else end", "comando");
        bad("while sem do", "while a write(1) end", "esperado 'do'");
        bad("repeat sem until", "repeat write(1)", "esperado 'until'");
        bad("unario duplo", "a = --1", "expressao");
        bad("operando ausente", "a = 1 +", "expressao");
        bad("parentese ausente", "write((1 + 2)", "esperado ')'" );
        bad("ampersand simples", "a = 1 & 2", "Erro lexico");
        bad("pipe simples", "a = 1 | 2", "Erro lexico");
        bad("numero com letra", "a = 1abc", "Erro lexico");
        bad("real incompleto", "a = 34.", "Erro lexico");
        bad("char vazio", "a = ''", "Erro lexico");
        bad("char longo", "a = 'AB'", "Erro lexico");
        bad("char nao ASCII", "a = '\u00e9'", "Erro lexico");
        bad("literal nao ASCII", "write(\"\u00e9\")", "Erro lexico");
        bad("literal nao fechado", "write(\"abc\n)", "Erro lexico (linha 1)");
        bad("simbolo invalido", "a = $", "Erro lexico");
        bad("comentario nao fechado", "{*abc", "Erro lexico (linha 1)");
        bad("abertura de comentario invalida", "{abc}", "Erro lexico");
        check("sem program", "p begin write(1) end.", "esperado 'program'");
        check("case sensitive keywords", "Program p begin write(1) end.", "esperado 'program'");
        check("sem nome", "program begin write(1) end.", "identificador");
        check("sem begin", "program p write(1) end.", "esperado 'begin'");
        check("sem end", "program p begin write(1)", "esperado 'end'");
        check("sem ponto", "program p begin write(1) end", "esperado '.'");
        check("tokens depois do ponto", "program p begin write(1) end. abc", "fim de arquivo");
        check("lexico depois do ponto", "program p begin write(1) end. $", "Erro lexico");
        check("arquivo vazio", "", "esperado 'program'");
        for (String newline : new String[]{"\n", "\r\n", "\r"}) {
            check("linha do token apos comentario", "program p" + newline + "begin" + newline
                + "{* c" + newline + "c *}write(1);" + newline + "end.", "linha 5");
            check("linha preservada no lookahead", "program p begin" + newline + "x" + newline
                + "=" + newline + "end.", "linha 4");
        }
        // Na etapa 2 nao se verifica declaracao nem compatibilidade de tipos.
        ok("semantica fora do escopo", "a = int; a = 'S'; indefinida = 1");
        System.out.println(count + " testes de regressao aprovados.");
        if (args.length == 1) {
            int files = 0;
            try (Stream<Path> paths = Files.walk(Paths.get(args[0], "Testes", "Etapa2"))) {
                Iterator<Path> iterator = paths.filter(p -> p.toString().endsWith(".txt")).iterator();
                while (iterator.hasNext()) {
                    Path path = iterator.next();
                    try (LexerEtapa2 lexer = new LexerEtapa2(path.toString())) {
                        while (lexer.scan().tag != LexerEtapa2.EOF) { /* validar todo o arquivo */ }
                    }
                    files++;
                }
            }
            System.out.println(files + " arquivos de teste sem erros lexicos.");
        }
    }
}
