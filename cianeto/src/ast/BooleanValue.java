/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class BooleanValue extends BasicValue{
	public BooleanValue(boolean value) {
		this.value = value;
	}
	public void genC( PW pw, boolean putParenthesis ) {
    	
    }
      // new method: the type of the expression
    public Type getType() {
    	return Type.booleanType;
    }
    
    private boolean value;

	public boolean getValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
    
}