package ast;

import lexer.Token;
public class RelationExpr extends Expr {
     
	public RelationExpr(SimpleExpr firstExpr, Token rel, SimpleExpr lastExpr) {
		this.firstExpr = firstExpr;
		this.lastExpr = lastExpr;
		this.rel = rel;
	}
	public void genC( PW pw, boolean putParenthesis ) {
    	 
     }
      // new method: the type of the expression
     public Type getType() {
    	 	return firstExpr.getType();
     }
     SimpleExpr firstExpr, lastExpr;
     Token rel;
}