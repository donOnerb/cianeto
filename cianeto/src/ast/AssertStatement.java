package ast;


public class AssertStatement extends Statement {
	public AssertStatement(Expr expr, String stringValue) {
		this.expr = expr;
		this.stringValue = stringValue;
	}
	public void genC(PW pw) {
		
	}
	Expr expr;
	String stringValue;
}
