package ast;

import java.util.*;
import lexer.Token;
public class Field {
	public Field(Token qualifier,Type type, ArrayList<String> idList) {
		this.type = type;
		this.idList = idList;
		this.qualifier = qualifier;
	}
	
	public Field() {
		this.type = null;
		this.idList = null;
		this.qualifier = null;
	}
	
	public Token getQualifier() {
		return qualifier;
	}
	public void setQualifier(Token qualifier) {
		this.qualifier = qualifier;
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

	Token qualifier;
	Type type;
	ArrayList<String> idList;
}
