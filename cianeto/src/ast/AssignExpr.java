/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class AssignExpr extends Statement {
	public AssignExpr(Expr expr, Expr anotherExpr) {
		this.expr = expr;
		this.anotherExpr = anotherExpr;
	}
	
	public void genC(PW pw) {
		
	}
	Expr expr, anotherExpr;
}
