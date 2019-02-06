/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class IfStatement extends Statement {
	public IfStatement(Expr expr, Statement statement, Statement elseStatement) {
		this.expr = expr; 
		this.statement = statement; 
		this.elseStatement = elseStatement;
	}
	public void genC(PW pw) {
		
	}
	Expr expr; 
	Statement statement, elseStatement;
}
