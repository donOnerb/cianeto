package ast;

import java.util.*;

import lexer.Token;
public class Method {
	public Method(ArrayList<ParamDec> paramDec,Type type, ArrayList<Statement> statementList,String id,Token qualifierEncapsulation, Token qualifierOverride, Token qualifierFinal) {
		this.paramDec = paramDec;
		this.type = type;
		this.statementList = statementList;
		this.id = id;
		this.qualifierEncapsulation = qualifierEncapsulation;
		this.qualifierOverride = qualifierOverride;
		this.qualifierFinal = qualifierFinal;
	}
	
	public Method(ArrayList<ParamDec> paramDec,Type type, String id,Token qualifierEncapsulation, Token qualifierOverride, Token qualifierFinal) {
		this.paramDec = paramDec;
		this.type = type;
		this.statementList = null;
		this.id = id;
		this.qualifierEncapsulation = qualifierEncapsulation;
		this.qualifierOverride = qualifierOverride;
		this.qualifierFinal = qualifierFinal;
	}
	
	public ArrayList<ParamDec> getParamDec() {
		return paramDec;
	}
	public void setParamDec(ArrayList<ParamDec> paramDec) {
		this.paramDec = paramDec;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public ArrayList<Statement> getStatementList() {
		return statementList;
	}
	public void setStatementList(ArrayList<Statement> statementList) {
		this.statementList = statementList;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Token getQualifierEncapsulation() {
		return qualifierEncapsulation;
	}
	public void setQualifierEncapsulation(Token qualifierEncapsulation) {
		this.qualifierEncapsulation = qualifierEncapsulation;
	}
	public Token getQualifierOverride() {
		return qualifierOverride;
	}
	public void setQualifierOverride(Token qualifierOverride) {
		this.qualifierOverride = qualifierOverride;
	}
	public Token getQualifierFinal() {
		return qualifierFinal;
	}
	public void setQualifierFinal(Token qualifierFinal) {
		this.qualifierFinal = qualifierFinal;
	}

	public boolean isPublic() {
		return  (this.qualifierEncapsulation == Token.PUBLIC) ? true : false;
	}
	
	public boolean isOverride() {
		return  (this.qualifierOverride == null) ? true : false;
	}
	
	public boolean isFinal() {
		return  (this.qualifierFinal == null) ? true : false;
	}


	private ArrayList<ParamDec> paramDec;
	private Type type;
	private ArrayList<Statement> statementList;
	private String id;
	private Token qualifierEncapsulation;
	private Token qualifierOverride; 
	private Token qualifierFinal;
}
