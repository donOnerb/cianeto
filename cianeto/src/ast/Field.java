package ast;

import java.util.*;

public class Field {
	public Field(Type type, ArrayList<String> idList) {
		this.type = type;
		this.idList = idList;
	}
	Type type;
	ArrayList<String> idList;
}
