/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

import lexer.Token;

public class FieldUnico {
	
	public FieldUnico(Type type, String id, Token qualifier) {
		this.type = type;
		this.id = id;
		this.qualifier = qualifier;
	}

	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Token getQualifier() {
		return qualifier;
	}
	public void setQualifier(Token qualifier) {
		this.qualifier = qualifier;
	}



	private Type type;
	private String id;
	private Token qualifier;
}
