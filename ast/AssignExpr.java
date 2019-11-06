package ast;

public class AssignExpr extends Statement {
	private Expr left, right;
	
    public AssignExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public void genJava(PW pw) {
    	this.left.genJava(pw);
        pw.print(" = ");
        this.right.genJava(pw);
        pw.println(";");
	}
}