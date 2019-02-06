/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;
import java.util.*;

public class WhileStatement extends Statement {
	public WhileStatement(Expr expr, ArrayList<Statement> statementList) {
		this.expr = expr; 
		this.statementList = statementList; 
	}
	public void genC(PW pw) {
		
	}
	Expr expr; 
	ArrayList<Statement> statementList;
}
