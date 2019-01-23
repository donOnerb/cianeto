package ast;

import java.util.*;	
/*
 * Krakatoa Class
 */
public class CianetoClass extends Type {

	public CianetoClass( String name ) {
		super(name);
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

	
	//private String name;
	private CianetoClass superclass;
	private ArrayList<Field> fieldList;
	private ArrayList<Method> publicMethodList, privateMethodList;

	// m�todos p�blicos get e set para obter e iniciar as vari�veis acima,
	// entre outros m�todos
}
