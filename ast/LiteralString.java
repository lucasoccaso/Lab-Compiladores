// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package ast;

public class LiteralString extends Expr {
    
    public LiteralString( String literalString ) { 
        this.literalString = literalString;
    }
    
    public void genJava( PW pw, boolean putParenthesis ) {
        pw.print(literalString);
    }

    
    public Type getType() {
        return Type.stringType;
    }
    
    private String literalString;
}
