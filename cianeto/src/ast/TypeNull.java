/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class TypeNull extends Type {

	public TypeNull() {
		super("NullType");
	}

	@Override
	public String getCname() {
		return "NULL";
	}

}
