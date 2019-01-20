package ast;

import java.util.*;
import lexer.Token;

public class Term {
     
	public Term(SignalFactor firstSigFac, ArrayList<Token> listOp, ArrayList<SignalFactor> listSigFac) {
		this.firstSigFac = firstSigFac;
		this.listOp = listOp;
		this.listSigFac = listSigFac;
	}
	/*public void genC( PW pw, boolean putParenthesis ) {
    	 
     }
      // new method: the type of the expression
     public Type getType() {
    	 	//return new Type();
     }*/
	SignalFactor firstSigFac;
	ArrayList<Token> listOp;
	ArrayList<SignalFactor> listSigFac;
}