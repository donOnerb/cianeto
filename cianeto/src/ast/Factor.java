/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class Factor {
	public Factor(BasicValue value, Expr expr, Factor fac,NullExpr nill, ObjectCreation obj, PrimaryExpr primaryExpr) {
		this.value = value;
		this.expr = expr;
		this.fac = fac;
		this.nill = nill;
		this.obj = obj;
		this.primaryExpr = primaryExpr;
	}
    /*abstract public void genC( PW pw, boolean putParenthesis );
      // new method: the type of the expression*/
    public Type getType(){
    	if(value != null)
    		return value.getType();
    	else if(expr != null)
    		return expr.getType();
    	else if(fac != null)
    		return fac.getType();
    	else if(nill != null)
    		return nill.getType();
    	else if(obj != null)
    		return obj.getType();
    	else
    		return primaryExpr.getType();
    	
    		
    }
	BasicValue value;
	Expr expr;
	Factor fac;
	NullExpr nill;
	ObjectCreation obj;
	PrimaryExpr primaryExpr;
}