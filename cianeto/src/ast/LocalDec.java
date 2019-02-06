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
	
	public Expr getExpr() {
		return expr;
	}
	
	public void setExpr(Expr expr) {
		this.expr = expr;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public ArrayList<String> getIdList() {
		return idList;
	}
	
	public void setIdList(ArrayList<String> idList) {
		this.idList = idList;
	}

	private Expr expr;
	private Type type;
	private ArrayList<String> idList;
}
