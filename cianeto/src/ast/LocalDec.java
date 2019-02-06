/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

import java.util.*;

public class LocalDec extends Statement {
	public LocalDec(Expr expr, Type type, ArrayList<String> idList) {
		this.expr = expr;
		this.type = type;
		this.idList = idList;
	}
	public void genC(PW pw) {
		
	}
	Expr expr;
	Type type;
	ArrayList<String> idList;
}
