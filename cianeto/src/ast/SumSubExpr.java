package ast;

import java.util.*;
import lexer.Token;
public class SumSubExpr {
     
	public SumSubExpr(Term firstTerm, ArrayList<Token> listOp, ArrayList<Term> listTerm) {
		this.firstTerm = firstTerm;
		this.listOp = listOp;
		this.listTerm = listTerm;
	}
	/*public void genC( PW pw, boolean putParenthesis ) {
    	 
     }*/
      // new method: the type of the expression
     public Type getType() {
    	return ();
     }
	Term firstTerm;
	ArrayList<Token> listOp;
	ArrayList<Term> listTerm;
}