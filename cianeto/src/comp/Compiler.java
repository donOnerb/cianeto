
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;

import ast.*;

import lexer.Lexer;
import lexer.Token;

public class Compiler {

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(char[] input, PrintWriter outError) {
		
		returnRequiredFlag = false;
		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);
		
		Program program = null;
		programClassExists = false;
		currentClass = null;
		
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
		
		try {
			if (!programClassExists)
				signalError.showError("Source code without a class 'Program'", true);
		} catch (CompilerError e) {
			e.printStackTrace();
			return program;
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
		boolean open = false;
		
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			next();
			open = true;
		}
		
		if ( lexer.token != Token.CLASS ) error("'class' expected and get " +lexer.token);
		lexer.nextToken();
		if ( lexer.token != Token.ID )
			error("Identifier expected");
		
		String className = lexer.getStringValue();
		currentClass = new String(className);
		
		/* Verifica se classe ainda não existe (Verifica na hash global por ser uma classe)
		*/
		Object type;
		
		if ((type = symbolTable.getInGlobal(className)) != null){
			if ((type instanceof CianetoClass)){
				error("Class already declared");
			}
		} 
		
		CianetoClass classe = new CianetoClass(className);
		classe.setOpen(open);
		
		if (classe.getName().equals("Program"))
			programClassExists = true;
		
		/* Coloca a classe na hash global
		*/
		symbolTable.putInGlobal(className, classe);
		
		lexer.nextToken();
		if ( lexer.token == Token.EXTENDS ) {
			lexer.nextToken();
			if ( lexer.token != Token.ID )
				error("Identifier expected");
			String superclassName = lexer.getStringValue();
			
			// Verifica se a classe está herdando de si mesma
			if (classe.getName().equals(superclassName)) {
				error("Class " + classe.getName() + " is inheriting from itself");
			}
			
			//
			
			/* Verifica se a superclasse existe e se é possível herdar da superclasse (se ela é open)
			*/
			if ((type = symbolTable.getInGlobal(superclassName)) != null){
				if ((type instanceof CianetoClass)){
					
				}
			} 
			
			lexer.nextToken();
		}
		//System.out.println(lexer.token);
		memberList(classe);
		//System.out.println(lexer.token);
		if ( lexer.token != Token.END) {
			error("'end' expected and get " +lexer.token);
		}
		
		// Verifica se existe o metodo 'run' caso seja a classe atual seja a 'Program'
		if (currentClass.equals("Program")){
			ArrayList<Method> listaMetodos = classe.getPublicMethodList();
			
			boolean existeRun = false;
			for (Method method : listaMetodos) {
				if (method.getId().equals("run")){
					existeRun = true;
					break;
				}	
			}
			if(!existeRun)
				error("Method 'run' was not found in class 'Program'");
		}
		
		
		lexer.nextToken();
		currentClass = null;
	}

	private void memberList(CianetoClass classe) {
		ArrayList<Field> campos = new ArrayList<Field>();
		ArrayList<Method> metodosPublicos = new ArrayList<Method>();
		ArrayList<Method> metodosPrivados = new ArrayList<Method>();
		Method metodo;
		
		while ( true ) {
			
			ArrayList<Token> qualifiers = qualifier();
			if ( lexer.token == Token.VAR ) {
				Field campo = fieldDec(qualifiers);
				
				// Verifica se field não voi declarado público
				if (campo.getQualifier() == Token.PUBLIC)
					signalError.showError("Attempt to declare public instance variable", true);
				
				campos.add(campo);
			}
			else if ( lexer.token == Token.FUNC ) {
				
				metodo = methodDec(qualifiers);
				if (metodo.isPublic())
					metodosPublicos.add(metodo);
				else 
					metodosPrivados.add(metodo);
			}
			else {
				break;
			}
		}
		
		classe.setFieldList(campos);
		classe.setPrivateMethodList(metodosPrivados);
		classe.setPublicMethodList(metodosPublicos);
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

	private Method methodDec(ArrayList<Token> qualifiers) {
		lexer.nextToken();
		
		ArrayList<ParamDec> parametros = null;
		Type tipo = null; 
		String nomeMetodo = null;
		
		if ( lexer.token == Token.IDCOLON ) {
				// keyword method. It has parameters
				// Retira o ':' do nome do metodo
				nomeMetodo = lexer.getStringValue();
				nomeMetodo = nomeMetodo.substring(0, nomeMetodo.length() - 1);
				
				// Verifica se o método run de Program possui parâmetros
				if (currentClass.equals("Program") && nomeMetodo.equals("run"))
					error("Method 'run:' of class 'Program' cannot take parameters");
				
				next();
				parametros = formalParamDec();
			
		//System.out.println(lexer.token+" AQUI");
		}else if ( lexer.token == Token.ID ) {
			nomeMetodo = lexer.getStringValue();
			
			// Verifica se o metodo 'run' da classe 'Program' não é private
			if (currentClass.equals("Program") && nomeMetodo.equals("run") && qualifiers.contains(Token.PRIVATE))
				error("Method 'run' of class 'Program' cannot be private");
			
			// unary method
			lexer.nextToken();
		}
		
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			if (currentClass.equals("Program") && nomeMetodo.equals("run"))
				error("Method 'run' of class 'Program' with a return value type");
			
			lexer.nextToken();
			returnRequiredFlag = true;
			tipo = type();
		}
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		next();
		statementList();
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}
		
		if ( returnRequiredFlag ) {
			error("missing 'return' statement");
		}
		
		returnRequiredFlag = false;
		
		next();
	
		Token qualifierEncapsulation = Token.PUBLIC;
		Token qualifierOverride = null;
		Token qualifierFinal = null;
	
		try{
			if (qualifiers.get(0) != null)
				qualifierEncapsulation = qualifiers.get(0);
		} catch (Exception e) {}
		
		try {
			if (qualifiers.get(1) != null)
				qualifierOverride = qualifiers.get(1);
		}  catch (Exception e) {}
		
		try {
		if (qualifiers.get(2) != null)
			qualifierFinal = qualifiers.get(2);
		}  catch (Exception e) {}
	
		Method novo = new Method(parametros, tipo, nomeMetodo, qualifierEncapsulation, qualifierOverride, qualifierFinal);
		
		return novo;
	
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
			//this.returnRequiredFlag = false;
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
				checkSemiColon = true;
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
			check(Token.SEMICOLON, "';' missing");
			//signalError.showError("';' missing", true);
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
		returnRequiredFlag = false;
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
		next();
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				statement();
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected and get " +lexer.token);
			next();
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
					
					if(lexer.getStringValue().equals("new:")) {
						error("')' expected");	
					}
					
					if(lexer.token == Token.NEW) {
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
				}
				
				break;
				
				
			case LITERALINT:
			case LITERALSTRING:
			case TRUE:
			case FALSE:
				next();
				break;
			default:
				if(lexer.getStringValue().equals("nil")) {
					next();
					break;
				}else if(lexer.getStringValue().equals("In")) {
					next();
					if(lexer.token == Token.DOT) {
						next();
						if(lexer.getStringValue().equals("readInt") || lexer.getStringValue().equals("readString")) {
							next();
						}else {
							error("'readInt' or 'readString' was expected and get " +lexer.token);
						}
					}else {
						error("'.' was expected and get " +lexer.token);
					}
					break;
				}else if(lexer.getStringValue().equals("self")) {
					
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
				}else if(lexer.getStringValue().equals("super")) {
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
	
	private ArrayList<ParamDec> formalParamDec() {
		ArrayList<ParamDec> parametros = new ArrayList<ParamDec>();
		parametros.add(paramDec());
		while(lexer.token == Token.COMMA){
			next();
			parametros.add(paramDec());
		}
		
		return parametros;
	}
	private ParamDec paramDec() {
		Type tipo = type();
		String identificador = null;
		if(lexer.token == Token.ID) {
			identificador = lexer.getStringValue();
			next();
		}else {
			error("A parameter name was expected and get " +lexer.token);
		}
		
		return new ParamDec(tipo, identificador);
	}
	private Field fieldDec(ArrayList<Token> qualifiers) {
		boolean flagVirgula = false;
		
		lexer.nextToken(); 
		Type tipo = type();
		ArrayList<String> idList = new ArrayList<String>();
		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected and get " +lexer.token);
		}
		else {
			while ( lexer.token == Token.ID ) {
				flagVirgula = false;
				idList.add(lexer.getStringValue());
				
				lexer.nextToken();
				
				if ( lexer.token == Token.COMMA ) {
					flagVirgula = true;
					lexer.nextToken();
				} else if( lexer.token == Token.SEMICOLON ) {
					next();
					break;
				} else {
					break;
				}
			}
			
			if (flagVirgula) {
				error("esperado 'id', mas recebido ';'");
			}
			
			
		}

		Field campo = new Field();
		campo.setType(tipo);
		campo.setIdList(idList);
		
		for (Token qualify : qualifiers) {
			if (qualify == Token.PUBLIC){
				campo.setQualifier(qualify);
			}
			
			if (qualify == Token.PRIVATE){
				campo.setQualifier(qualify);
			}
		}
		qualifiers.add(Token.PRIVATE);
			
		return(campo);
	}

	private Type type() {
		Type tipo = null;
		
		if ( lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING ) {
			switch(lexer.token) {
				case INT:
					tipo = Type.intType;
					break;
				case BOOLEAN:
					tipo = Type.booleanType;
					break;
				case STRING:
					tipo = Type.stringType;
					break;
			}

			lexer.nextToken();
		}
		else if ( lexer.token == Token.ID ) {
			tipo = new CianetoClass(lexer.getLiteralStringValue());
			lexer.nextToken();
		}
		else {
			this.error("A type was expected and get " +lexer.token);
		}
		return (tipo);
	}


	private ArrayList<Token> qualifier() {
		ArrayList<Token> qualifiers = new ArrayList<Token>();
		
		if ( lexer.token == Token.PRIVATE ) {
			qualifiers.add(Token.PRIVATE);
			next();
		}
		else if ( lexer.token == Token.PUBLIC ) {
			qualifiers.add(Token.PUBLIC);
			next();
		}
		else if ( lexer.token == Token.OVERRIDE ) {
			qualifiers.add(Token.OVERRIDE);
			next();
			if ( lexer.token == Token.PUBLIC ) {
				qualifiers.add(Token.PUBLIC);
				next();
			}
		}
		else if ( lexer.token == Token.FINAL ) {
			qualifiers.add(Token.FINAL);
			next();
			if ( lexer.token == Token.PUBLIC ) {
				qualifiers.add(Token.PUBLIC);
				next();
			}
			else if ( lexer.token == Token.OVERRIDE ) {
				qualifiers.add(Token.OVERRIDE);
				next();
				if ( lexer.token == Token.PUBLIC ) {
					qualifiers.add(Token.PUBLIC);
					next();
				}
			}
		}
		
		return qualifiers;
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
	private boolean returnRequiredFlag; 
	private boolean programClassExists;
	private String currentClass;
	

}
