/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class WriteStatement extends Statement {
	public WriteStatement(Expr expr) {
		this.expr = expr; 
	}
	public void genC(PW pw) {
		
	}
	Expr expr; 
}
