
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import ast.CianetoClass;
import ast.LiteralInt;
import ast.MetaobjectAnnotation;
import ast.Program;
import ast.Statement;
import lexer.Lexer;
import lexer.Token;

public class Compiler {

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(char[] input, PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);

		Program program = null;
		lexer.nextToken();
		program = program(compilationErrorList);
		return program;
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		// Program ::= CianetoClass { CianetoClass }
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		ArrayList<CianetoClass> CianetoClassList = new ArrayList<>();
		Program program = new Program(CianetoClassList, metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		while ( lexer.token == Token.CLASS ||
				(lexer.token == Token.ID && lexer.getStringValue().equals("open") ) ||
				lexer.token == Token.ANNOT ) {
			try {
				while ( lexer.token == Token.ANNOT ) {
					metaobjectAnnotation(metaobjectCallList);
				}
				classDec();
			}
			catch( CompilerError e) {
				// if there was an exception, there is a compilation error
				thereWasAnError = true;
				while ( lexer.token != Token.CLASS && lexer.token != Token.EOF ) {
					try {
						next();
					}
					catch ( RuntimeException ee ) {
						e.printStackTrace();
						return program;
					}
				}
			}
			catch ( RuntimeException e ) {
				e.printStackTrace();
				thereWasAnError = true;
			}

		}
		if ( !thereWasAnError && lexer.token != Token.EOF ) {
			try {
				error("End of file expected and get " +lexer.token);
			}
			catch( CompilerError e) {
			}
		}
		return program;
	}

	/**  parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     * @cep(5, "'class' expected and get " +lexer.token) <br>
     * class Program <br>
     *     func run { } <br>
     * end <br>
     * </code>
     *

	 */
	@SuppressWarnings("incomplete-switch")
	private void metaobjectAnnotation(ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
		String name = lexer.getMetaobjectName();
		int lineNumber = lexer.getLineNumber();
		lexer.nextToken();
		ArrayList<Object> metaobjectParamList = new ArrayList<>();
		boolean getNextToken = false;
		if ( lexer.token == Token.LEFTPAR ) {
			// metaobject call with parameters
			lexer.nextToken();
			while ( lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING ||
					lexer.token == Token.ID ) {
				switch ( lexer.token ) {
				case LITERALINT:
					metaobjectParamList.add(lexer.getNumberValue());
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.getLiteralStringValue());
					break;
				case ID:
					metaobjectParamList.add(lexer.getStringValue());
				}
				lexer.nextToken();
				if ( lexer.token == Token.COMMA )
					lexer.nextToken();
				else
					break;
			}
			if ( lexer.token != Token.RIGHTPAR )
				error("')' expected after metaobject call with parameters");
			else {
				getNextToken = true;
			}
		}
		if ( name.equals("nce") ) {
			if ( metaobjectParamList.size() != 0 )
				error("Metaobject 'nce' does not take parameters");
		}
		else if ( name.equals("cep") ) {
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				error("Metaobject 'cep' take three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  ) {
				error("The first parameter of metaobject 'cep' should be an integer number");
			}
			else {
				int ln = (Integer ) metaobjectParamList.get(0);
				metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				error("The second and third parameters of metaobject 'cep' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )
				error("The fourth parameter of metaobject 'cep' should be a literal string");

		}
		metaobjectAnnotationList.add(new MetaobjectAnnotation(name, metaobjectParamList));
		if ( getNextToken ) lexer.nextToken();
	}

	private void classDec() {
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			// open class
		}
		
		if ( lexer.token != Token.CLASS ) error("'class' expected and get " +lexer.token);
		lexer.nextToken();
		if ( lexer.token != Token.ID )
			error("Identifier expected");
		String className = lexer.getStringValue();
		lexer.nextToken();
		if ( lexer.token == Token.EXTENDS ) {
			//System.out.println("not enter");
			lexer.nextToken();
			if ( lexer.token != Token.ID )
				error("Identifier expected");
			String superclassName = lexer.getStringValue();

			lexer.nextToken();
		}
		//System.out.println(lexer.token);
		memberList();
		//System.out.println(lexer.token);
		if ( lexer.token != Token.END) {
			error("'end' expected and get " +lexer.token);
		}
		lexer.nextToken();

	}

	private void memberList() {
		while ( true ) {
			
			qualifier();
			if ( lexer.token == Token.VAR ) {
				//System.out.println("aaaaaaaaaaaaaaaaaaaaaa");
				fieldDec();
			}
			else if ( lexer.token == Token.FUNC ) {
				
				methodDec();
			}
			else {
				break;
			}
		}
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}

	private void next() {
		lexer.nextToken();
	}

	private void check(Token shouldBe, String msg) {
		if ( lexer.token != shouldBe ) {
			error(msg);
		}
	}

	private void methodDec() {
		lexer.nextToken();
		 if ( lexer.token == Token.IDCOLON ) {
				// keyword method. It has parameters
				next();
				formalParamDec();

			
		//System.out.println(lexer.token+" AQUI");
		 }else if ( lexer.token == Token.ID ) {
			// unary method
			lexer.nextToken();
		}
		
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			lexer.nextToken();
			type();
		}
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		next();
		statementList();
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}
		
		next();

	}

	private void statementList() {
		  // only '}' is necessary in this test
		while ( lexer.token != Token.RIGHTCURBRACKET) {
			statement();
		}
	}

	private void statement() {
		boolean checkSemiColon = true;
		switch ( lexer.token ) {
		case IF:
			ifStat();
			checkSemiColon = false;
			break;
		case WHILE:
			whileStat();
			checkSemiColon = false;
			break;
		case RETURN:
			returnStat();
			//checkSemiColon = false;
			break;
		case BREAK:
			breakStat();
			break;
		case SEMICOLON:
			next();
			break;
		case REPEAT:
			repeatStat();
			break;
		case VAR:
			localDec();
			break;
		case ASSERT:
			assertStat();
			break;
		default:
			
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("out") ) {
				writeStat();
			} else {
				//System.out.println("Com =");
				
				expr();
				if(lexer.token == Token.ASSIGN) {
					next();
					//System.out.println("Com =");
					expr();
					//System.out.println("Com =");
				}
			}

		}
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected and get " +lexer.token);
			next();
		}
	}

	private void localDec() {
		boolean flag = true;
		
		next();
		type();
		check(Token.ID, "A variable name was expected and get " + lexer.token);
		while ( lexer.token == Token.ID ) {
			flag = false;
			next();
			if ( lexer.token == Token.COMMA ) {
				flag = true;
				next();
			}
			else {
				break;
			}
		}
		
		if (flag) {
			error("Missing identifier");
		}
		
		if ( lexer.token == Token.ASSIGN ) {
			next();
			// check if there is just one variable
			expr();
		}

	}

	private void repeatStat() {
		next();
		while ( lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
		check(Token.UNTIL, "'until' was expected and get " +lexer.token);
	}

	private void breakStat() {
		next();

	}

	private void returnStat() {
		//System.out.println("NEM AQUI TO ENTRANDO");
		next();
		expr();
	}

	private void whileStat() {
		next();
		expr();
		check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected and get " +lexer.token);
		next();
	}

	private void ifStat() {
		next();
		expr();
		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE ) {
			statement();
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected and get " +lexer.token);
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				statement();
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected and get " +lexer.token);
		}
	}

	/**

	 */
	private void writeStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'out.'");
		String printName = lexer.getStringValue();
		expr();
	}

	private void expr() {
		simpleExpr();
		if(lexer.token == Token.EQ ||
		   lexer.token == Token.LT || 
		   lexer.token == Token.GT || 
		   lexer.token == Token.LE || 
		   lexer.token == Token.GE || 
		   lexer.token == Token.NEQ) {
			next();
			simpleExpr();
		}
		//next();
	}
	
	private void simpleExpr() {
		sumSubExpr();
		while(lexer.token == Token.PLUS) {
			next();
			if(lexer.token == Token.PLUS) {
				next();
			}else {
				error("'++' was expected and get " +lexer.token);
			}
			sumSubExpr();
			
		}
		//next();
	}
	
	private void sumSubExpr() {
		term();
		while(lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
			next();
			term();
		}
		//next();
	}
	
	private void term() {
		if(lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
			next();
		}
		factor();
		while(lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
			next();
			if(lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
				next();
			}
			factor();
		}
			//next();
		
	}
	
	private void factor() {
		switch(lexer.token) {
			case LEFTPAR:
				next();
				expr();
				if(lexer.token == Token.RIGHTPAR)
					next();
				else
					error("'(' was expected and get " +lexer.token);
				break;
			case NOT:
				next();
				factor();
				break;
			case ID:
				next();
				if(lexer.token == Token.DOT) {
					next();
					if(lexer.token.toString().equals("new")) {
						next();
						break;
					}else if(lexer.token == Token.ID) {
						next();
						break;
					}else if(lexer.token == Token.IDCOLON) {
						next();
						expressionList();
						break;
					}else {
						error("'new', identifier or identifiercolon were expected and get " +lexer.token);
					}
				} else if(lexer.token != Token.MULT && lexer.token != Token.DIV && lexer.token != Token.AND) {
					error("Statement expected" + lexer.token);
				}
				
				break;
				
				
			case LITERALINT:
			case LITERALSTRING:
			case TRUE:
			case FALSE:
				next();
				break;
			default:
				if(lexer.token.toString().equals("nil")) {
					next();
					break;
				}else if(lexer.token.toString().equals("In")) {
					next();
					if(lexer.token == Token.DOT) {
						next();
						if(lexer.token.toString().equals("readInt") || lexer.token.toString().equals("readString")) {
							next();
						}else {
							error("'readInt' or 'readString' was expected and get " +lexer.token);
						}
					}else {
						error("'.' was expected and get " +lexer.token);
					}
					break;
				}else if(lexer.token.toString().equals("self")) {
					
					next();
					
					if(lexer.token == Token.DOT) {
						next();
						if(lexer.token == Token.ID) {
							next();
							if(lexer.token == Token.DOT) {
								next();
								if(lexer.token == Token.ID) {
									next();
								}else if(lexer.token == Token.IDCOLON) {
									next();
									expressionList();
								}else {
									error("identifier or identifeircolon was expected and get " +lexer.token);
								}
							}
							
						}else if(lexer.token == Token.IDCOLON) {
							next();
							expressionList();
						}
						
					}
					
					break;
				}else if(lexer.token.toString().equals("super")) {
					next();
					if(lexer.token == Token.DOT) {
						next();
						if(lexer.token == Token.ID) {
							next();
							break;
						}else if(lexer.token == Token.IDCOLON) {
							next();
							expressionList();
							break;
						}else {
							error("identifier or identifiercolon was expected and get " +lexer.token);
						}
							
					}else {
						error("'.' was expected and get " +lexer.token);
					}
					
					break;
				}
			
				error("a lot of thing was expected and get " +lexer.token);
				
			
		}
		
	}
	
	private void expressionList() {
		expr();
		while(lexer.token == Token.COMMA) {
			next();
			expr();
		}
		
		
	}
	
	private void formalParamDec() {
		paramDec();
		while(lexer.token == Token.COMMA){
			next();
			paramDec();
		}
	}
	private void paramDec() {
		type();
		if(lexer.token == Token.ID) {
			next();
		}else {
			error("A parameter name was expected and get " +lexer.token);
		}
	}
	private void fieldDec() {
		lexer.nextToken();
		type();
		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected and get " +lexer.token);
		}
		else {
			while ( lexer.token == Token.ID  ) {
				lexer.nextToken();
				
				if ( lexer.token == Token.COMMA ) {
					lexer.nextToken();
				} /*else if (lexer.token == Token.SEMICOLON) {
					next();
					break;
				}*/else {
					break;
				}
			}
		}

	}

	private void type() {
		if ( lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING ) {
			lexer.nextToken();
		}
		else if ( lexer.token == Token.ID ) {
			lexer.nextToken();
		}
		else {
			this.error("A type was expected and get " +lexer.token);
		}

	}


	private void qualifier() {
		if ( lexer.token == Token.PRIVATE ) {
			next();
		}
		else if ( lexer.token == Token.PUBLIC ) {
			next();
		}
		else if ( lexer.token == Token.OVERRIDE ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
		}
		else if ( lexer.token == Token.FINAL ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
			else if ( lexer.token == Token.OVERRIDE ) {
				next();
				if ( lexer.token == Token.PUBLIC ) {
					next();
				}
			}
		}
	}
	/**
	 * change this method to 'private'.
	 * uncomment it
	 * implement the methods it calls
	 */
	public Statement assertStat() {

		lexer.nextToken();
		int lineNumber = lexer.getLineNumber();
		expr();
		if ( lexer.token != Token.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( lexer.token != Token.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.getLiteralStringValue();
		lexer.nextToken();
		if ( lexer.token == Token.SEMICOLON )
			lexer.nextToken();

		return null;
	}




	private LiteralInt literalInt() {

		LiteralInt e = null;

		// the number value is stored in lexer.getToken().value as an object of
		// Integer.
		// Method intValue returns that value as an value of type int.
		int value = lexer.getNumberValue();
		lexer.nextToken();
		return new LiteralInt(value);
	}

	private static boolean startExpr(Token token) {

		return token == Token.FALSE || token == Token.TRUE
				|| token == Token.NOT || token == Token.SELF
				|| token == Token.LITERALINT || token == Token.SUPER
				|| token == Token.LEFTPAR || token == Token.NULL
				|| token == Token.ID || token == Token.LITERALSTRING;

	}

	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
