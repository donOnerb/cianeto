/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

import java.util.*;
public class SimpleExpr extends Expr {
     
	public SimpleExpr(SumSubExpr firstExpr, ArrayList<SumSubExpr> listExpr) {
		this.firstExpr = firstExpr;
		this.listExpr = listExpr;
	}
	public void genC( PW pw, boolean putParenthesis ) {
    	 
     }
      // new method: the type of the expression
     public Type getType() {
    	 	return firstExpr.getType();
     }
     SumSubExpr firstExpr;
     ArrayList<SumSubExpr> listExpr;
}