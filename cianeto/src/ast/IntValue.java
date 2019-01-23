package ast;

public class IntValue extends BasicValue{
	public IntValue(int value) {
		this.value = value;
	}
	public void genC( PW pw, boolean putParenthesis ) {
    	
    }
      // new method: the type of the expression
    public Type getType() {
    	return Type.intType;
    }
    
    private int value;

	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
    
}