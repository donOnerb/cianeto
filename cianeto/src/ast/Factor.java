package ast;

public class Factor {
	public Factor(BasicValue value, Expr expr, Factor fac, ObjectCreation obj, PrimaryExpr primaryExpr) {
		this.value = value;
		this.expr = expr;
		this.fac = fac;
		this.obj = obj;
		this.primaryExpr = primaryExpr;
	}
    /*abstract public void genC( PW pw, boolean putParenthesis );
      // new method: the type of the expression
    abstract public Type getType();*/
	BasicValue value;
	Expr expr;
	Factor fac;
	ObjectCreation obj;
	PrimaryExpr primaryExpr;
}