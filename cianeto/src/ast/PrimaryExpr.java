package ast;

import java.util.*;

public class PrimaryExpr {
	public PrimaryExpr(String idColon, String firstId, String LastId, ArrayList<Expr> listExpr, ReadExpr readExpr) {
		this.idColon = idColon;
		this.firstId = firstId; 
		this.LastId = LastId; 
		this.listExpr = listExpr;
		this.readExpr = readExpr;
	}
    /*abstract public void genC( PW pw, boolean putParenthesis );
      // new method: the type of the expression
    abstract public Type getType();*/
	String idColon;
	String firstId; 
	String LastId; 
	ArrayList<Expr> listExpr;
	ReadExpr readExpr;
}