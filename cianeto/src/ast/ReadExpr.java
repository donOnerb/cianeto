package ast;

public class ReadExpr {
	public ReadExpr(String read) {
		this.read = read;
	}
	public Type getType() {
		if(this.read.equals("readInt"))
			return Type.intType;
		else
			return Type.stringType;
	}
	private String read; 
}
