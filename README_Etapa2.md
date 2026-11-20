# Etapa 2 - Analisador sintatico

Implementacao manual em Java para a gramatica de Compiladores 2026/2.

## Executar

Requer Java 8 ou superior. No diretorio do projeto:

```powershell
java -jar CompiladorEtapa2.jar Testes/Etapa2/Teste6A_final.txt
```

O analisador informa sucesso ou o primeiro erro lexico/sintatico com a linha. Retorna
0 no sucesso e 1 no erro. Nao exibe tokens ou tabela de simbolos. A entrada e UTF-8.

## Compilar e testar

Requer um JDK 9 ou superior com `javac` e `jar` disponiveis:

```powershell
.\CompilarEtapa2.ps1
.\TestarEtapa2.ps1
```

Para selecionar outro JDK, passe `-Javac "C:\caminho\jdk\bin\javac.exe"` aos scripts.
As classes ficam em `build/`, sem substituir as classes da etapa 1. O script compila
apenas os fontes necessarios e gera `CompiladorEtapa2.jar` com `Main-Class: Main`.

Alternativa sem PowerShell:

```text
javac -encoding UTF-8 --release 8 -d build/etapa2 Main.java LexerEtapa2.java ParserEtapa2.java SyntaxException.java Token.java Tag.java Word.java Literal.java CharConst.java
jar cfe CompiladorEtapa2.jar Main -C build/etapa2 .
```

## Arquivos para revisar

- `ParserEtapa2.java`: descida recursiva, tratamento de declaracoes opcionais,
  expressoes, todos os comandos e verificacao de fim do arquivo.
- `LexerEtapa2.java`: leitura exclusiva da etapa 2, com as palavras `read` e `write`,
  comentarios `{* ... *}`, linhas corretas e padroes lexicos do enunciado.
- `Main.java` e `SyntaxException.java`: entrada, saida, encerramento e diagnosticos.
- `Testes/Etapa2/`: cinco testes do enunciado, dois adicionais, versoes sucessivas,
  versoes finais, resultados em JSON e testes de regressao.

Os fontes, binarios e relatorio da etapa 1, assim como os testes anteriores, foram
preservados. A etapa 2 reutiliza as classes basicas sem altera-las. O antigo
`parser.java`, que nao compilava com os tokens e pacotes existentes, foi substituido
por `ParserEtapa2.java`. `Main.java` ja era o ponto de entrada do parser no ultimo
commit; agora esta integrado com a implementacao da etapa 2.

## Testes

Cada pasta `TesteN` guarda as versoes em ordem numerica. `00.txt` e a base com
erros sintaticos; a ultima versao deve ser aceita. Os arquivos `TesteN_final.txt`
facilitam executar as sete versoes finais. O arquivo `resultados.json` registra
as bases, as correcoes aplicadas e as saidas de cada execucao. Os arquivos
entregues da etapa 1 nao sao reescritos.

## Limites da etapa

A gramatica exige declaracoes dentro de `begin`, com `identificadores = tipo;`.
Listas de comandos nao podem ser vazias. `;` separa comandos e nao e aceito
imediatamente antes de `end`, `else` ou `until`. A linguagem distingue maiusculas
e minusculas. A atribuicao recebe uma expressao simples; comparacoes nessa
posicao precisam de parenteses, conforme a gramatica.

Verificacao de declaracoes, compatibilidade de tipos e geracao/execucao de codigo
pertencem a etapa 3. Os testes finais podem manter erros semanticos para essa etapa.
