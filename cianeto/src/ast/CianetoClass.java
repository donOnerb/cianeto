/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

import java.util.*;	
/*
 * Krakatoa Class
 */
public class CianetoClass extends Type {

	public CianetoClass( String name ) {
		super(name);
		this.superclass = null;
	}
	
	@Override
	public String getCname() {
		return getName();
	}

	public CianetoClass getSuperclass() {
		return superclass;
	}

	public void setSuperclass(CianetoClass superclass) {
		this.superclass = superclass;
	}

	public ArrayList<Field> getFieldList() {
		return fieldList;
	}

	public void setFieldList(ArrayList<Field> fieldList) {
		this.fieldList = fieldList;
	}

	public ArrayList<Method> getPublicMethodList() {
		return publicMethodList;
	}

	public void setPublicMethodList(ArrayList<Method> publicMethodList) {
		this.publicMethodList = publicMethodList;
	}

	public ArrayList<Method> getPrivateMethodList() {
		return privateMethodList;
	}

	public void setPrivateMethodList(ArrayList<Method> privateMethodList) {
		this.privateMethodList = privateMethodList;
	}

	public boolean getOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	//private String name;
	private CianetoClass superclass;
	private ArrayList<Field> fieldList;
	private ArrayList<Method> publicMethodList, privateMethodList;
	private boolean open;


	// métodos públicos get e set para obter e iniciar as variáveis acima,
	// entre outros métodos
}
