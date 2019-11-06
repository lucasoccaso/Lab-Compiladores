package ast;

public class AssertStat extends Statement{
    Expr expr;
    String string;

    public AssertStat(Expr expr, String string){
        this.expr = expr;
        this.string = string;
    }

    public void genJava(PW pw){
    	pw.print("assert ");
        this.expr.genJava(pw);
        pw.print(", \""  + string + "\";");
	}
}