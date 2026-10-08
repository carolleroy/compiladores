public class Tag {
    // Palavras reservadas
    public final static int 
        PRG = 256,
        BEG = 257,
        END = 258,
        INT = 260,
        CHAR = 261,
        FLOAT = 262,
        IF = 263,
        THEN = 264,
        ELSE = 265,
        WHILE = 266,
        DO = 267,
        REPEAT = 268,
        UNTIL = 269,
        IN = 270,
        OUT = 271;

    // Operadores compostos e especiais
    public final static int 
        EQ = 288, // ==
        GE = 289, // >=
        LE = 290, // <=
        NE = 291, // !=
        AND = 292,
        OR = 293,

        /*Colocamos o '=' aqui não por ser um símbolo composto
        mas por ter uma função específica na gramática: fazer 
        atribuição. Foi apenas uma decisão de implementação, 
        não era necessário.*/
        ASSIGN = 294;

    // Outros tokens básicos
    public final static int 
        NUM = 278,      //constante numérica
        ID = 279,       //identificador
        REAL = 280,     //constante real
        CHAR_CONST = 281,
        LITERAL = 282;
}
