package ast;

import java.util.ArrayList;

public class LocalDecUnit {
	
	public LocalDecUnit(Type type, String id) {
		this.type = type;
		this.id = id;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}


	private Type type; 
	private String id;
}
