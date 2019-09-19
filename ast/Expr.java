// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package ast;

abstract public class Expr extends Statement {
    abstract public void genC( PW pw, boolean putParenthesis );
	@Override
	public void genC(PW pw) {
		this.genC(pw, false);
	}

      // new method: the type of the expression
    abstract public Type getType();
}