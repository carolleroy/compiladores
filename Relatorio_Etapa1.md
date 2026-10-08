# Relatorio - Etapa 1: Analisador Lexico

## Forma de uso

Compile os arquivos Java:

```bash
javac *.java
```

Execute o analisador informando o caminho do programa fonte:

```bash
java Main Testes/TesteEtapa1Valido.txt
```

Na etapa 1, o compilador exibe a sequencia de tokens encontrada e, ao final, a tabela de simbolos.

## Abordagem utilizada

O analisador lexico foi implementado manualmente em Java, sem uso de geradores. A classe `Lexer` percorre o arquivo caractere por caractere, ignora espacos, quebras de linha e comentarios, reconhece palavras reservadas, identificadores, constantes, literais e operadores, e retorna objetos derivados de `Token`.

Principais classes:

- `Main`: recebe o arquivo fonte por parametro, chama o analisador lexico ate o fim do arquivo e imprime os tokens.
- `Lexer`: contem a logica de leitura e reconhecimento dos tokens.
- `Tag`: define os codigos numericos dos tokens compostos e palavras reservadas.
- `Token`: representa tokens simples, como pontuacao e operadores de um caractere.
- `Word`: representa palavras reservadas, operadores compostos e identificadores.
- `Num`: representa constantes inteiras.
- `Real`: representa constantes reais.
- `CharConst`: representa constantes de caractere.
- `Literal`: representa literais entre aspas duplas.

## Recursos implementados

- Palavras reservadas: `program`, `begin`, `end`, `int`, `float`, `char`, `if`, `then`, `else`, `while`, `do`, `repeat`, `until`, `in` e `out`.
- Identificadores no formato `(letter|_) (letter|digit|_)*`.
- Constantes inteiras, reais, de caractere e literais.
- Operadores relacionais `==`, `>`, `>=`, `<`, `<=` e `!=`.
- Operadores logicos `&&`, `||` e `!`.
- Comentarios de bloco `{ ... }` e comentarios de linha iniciados por `%`.
- Linguagem sem diferenciacao entre maiusculas e minusculas.
- Mensagens de erro lexico com numero da linha.

## Testes

### Teste valido

Arquivo: `Testes/TesteEtapa1Valido.txt`

Resultado esperado: o analisador exibe todos os tokens e a tabela de simbolos sem erro lexico.

### Teste com erro

Arquivo: `Testes/TesteEtapa1Erro.txt`

Resultado esperado: o analisador encontra o caractere invalido `$` na declaracao `area$` e exibe erro lexico indicando a linha correspondente.
