

package ast;
import lexer.Token;

public class CompositeExpr extends Expr {
	Expr left, right = null;
	Token oper = null;

	public CompositeExpr(Expr left, Token oper, Expr right){
		this.left = left;
		this.oper = oper;
		this.right = right;
	}
 
	public Type getType(){
		return left.getType();
	}

	public void genJava(PW pw, boolean putParenthesis) {
		left.genJava(pw);
		if(oper == null)
			return;
		if(this.oper == Token.OR)
			pw.print(" || ");
		else if(this.oper == Token.AND)
			pw.print(" && ");
		else
			pw.print(" " + this.oper.toString() + " ");
		right.genJava(pw);
	}
}