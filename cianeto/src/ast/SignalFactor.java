/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

import lexer.Token;
public class SignalFactor {
     
	public SignalFactor(Token signal, Factor factor) {
		this.signal = signal;
		this.factor = factor;
	}
	public void genC( PW pw, boolean putParenthesis ) {
    	 
     }
      // new method: the type of the expression
     public Type getType() {
    	 	return factor.getType();
     }
	Token signal;
	Factor factor;
	}