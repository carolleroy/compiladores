public class Tag {
    // Palavras reservadas
    public final static int 
        PRG = 256,
        BEG = 257,
        END = 258,
        TYPE = 259,
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
        READ = 270,
        WRITE = 271,

    // Operadores compostos e especiais
    public final static int 
        EQ = 288,
        GE = 289, // >=
        LE = 290,
        NE = 291, // !=
        AND = 292,
        OR = 293,
        ASSIGN = 294;

    // Outros tokens básicos
    public final static int 
        NUM = 278,
        ID = 279;
}