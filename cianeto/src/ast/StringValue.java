package ast;

public class StringValue extends BasicValue{
	public StringValue(String value) {
		this.value = value;
	}
	public void genC( PW pw, boolean putParenthesis ) {
    	
    }
      // new method: the type of the expression
    public Type getType() {
    	return Type.stringType;
    }
    
    private String value;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
    
}