package ast;

import java.util.*;
import lexer.Token;
public class Field {
	public Field(Token qualifier,Type type, ArrayList<String> idList) {
		this.type = type;
		this.idList = idList;
		this.qualifier = qualifier;
	}
	Token qualifier;
	Type type;
	ArrayList<String> idList;
}
