Autoras: Caroline Leroy, Clara Barbi, Julia Meireles.

Olá! Seja bem vindo ao nosso compilador!
Aqui estão alguns comandos necessários para fazer esse 
maravilhoso compilador funcionar:

-> Construindo o nosso compilador! Use o seguinte código:

javac -d build Main.java Lexer.java ParserEtapa2.java 
SyntaxException.java Token.java Tag.java Word.java 
Num.java Real.java Literal.java CharConst.java


-> Agora, vamos criar o arquivo .jar, que vai conter os 
arquivos compilados que estão dentro do build:

jar cfe CompiladorEtapa2.jar Main -C build .


-> Como executar esse .jar depois? Basta fazer:

java -jar CompiladorEtapa2.jar
