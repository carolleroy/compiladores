public class Token {
    public final int tag; // constante que representa o token

    public Token(int t) {
        tag = t;
    }

    /*Se tag < 256, é um caractere solto, então converte 
    de volta para char e imprime o caracter (';', ')', '+'...).
    Se tag >= 256, é uma tag da sua enumeração, e imprime o número 
    normalmente. */
    @Override
    public String toString() {
        return tag < 256 ? "" + (char) tag : "" + tag;
    }
}