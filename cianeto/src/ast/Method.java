package ast;

import java.util.*;

import lexer.Token;
public class Method {
	public Method(ArrayList<ParamDec> paramDec,Type type, ArrayList<Statement> statementList,String id,Token qualifier) {
		this.paramDec = paramDec;
		this.type = type;
		this.statementList = statementList;
		this.id = id;
		this.qualifier = qualifier;
	}
	ArrayList<ParamDec> paramDec;
	Type type;
	ArrayList<Statement> statementList;
	String id;
	Token qualifier;
}
