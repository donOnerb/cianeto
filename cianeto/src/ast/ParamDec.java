package ast;

public class ParamDec {
	public ParamDec(Type type, String id) {
		this.type = type;
		this.id = id;
	}
	
	Type type;
	String id;
}
