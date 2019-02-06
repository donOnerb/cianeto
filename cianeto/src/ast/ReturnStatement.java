/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class ReturnStatement extends Statement {
	public ReturnStatement(Expr expr) {
		this.expr = expr; 
	}
	public void genC(PW pw) {
		
	}
	Expr expr; 
}
