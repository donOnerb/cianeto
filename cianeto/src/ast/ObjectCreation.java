/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class ObjectCreation {
	public ObjectCreation(String id) {
		this.id = id;
	}
	public Type getType() {
		return Type.undefinedType;
	}
	public String getIdentifier() {
		return id;
	}
    /*abstract public void genC( PW pw, boolean putParenthesis );
      // new method: the type of the expression
    abstract public Type getType();*/
	private String id;
}