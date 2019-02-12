/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/

package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import ast.CianetoClass;
import ast.Field;
import ast.LiteralInt;
import ast.LocalDec;
import ast.MetaobjectAnnotation;
import ast.Method;
import ast.ParamDec;
import ast.Program;
import ast.Statement;
import ast.Type;
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
		metodoAtual = null;
		countWhile = 0;
		countRepeat = 0;
		metodoTemRetorno = false;
		countReturns = 0;
		
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
			
			/* Verifica se a superclasse existe e se é possível herdar da superclasse (se ela é open)
			*/
			if ((type = symbolTable.getInGlobal(superclassName)) != null){
				if ((type instanceof CianetoClass)){
					CianetoClass classeNova = (CianetoClass)type;
					if (!classeNova.getOpen())
						error("Impossible to inherit class " + classeNova.getName() + " because it is not open");
						
					classe.setSuperclass(classeNova);			
					
				}
			} else {
				error("Superclass doesn't exists");
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
		
		symbolTable.removeLocalClassIdent();
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
				
				// Verifica se field não vai declarado público
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
		
		Token qualifierEncapsulation = Token.PUBLIC;
		Token qualifierOverride = null;
		Token qualifierFinal = null;
		Method novo;

		if ( lexer.token == Token.IDCOLON ) {
				// keyword method. It has parameters
				// Retira o ':' do nome do metodo
				nomeMetodo = lexer.getStringValue();
				nomeMetodo = nomeMetodo.substring(0, nomeMetodo.length() - 1);
				metodoAtual = nomeMetodo;
				
				// Verifica se o método run de Program possui parâmetros
				if (currentClass.equals("Program") && nomeMetodo.equals("run"))
					error("Method 'run:' of class 'Program' cannot take parameters");
				
				next();
				parametros = formalParamDec();
			
		//System.out.println(lexer.token+" AQUI");
		}else if ( lexer.token == Token.ID ) {
			nomeMetodo = lexer.getStringValue();
			metodoAtual = nomeMetodo;
			
			// Verifica se o método está sendo sobrescrito
			Object type;
			if ((type = symbolTable.getInLocalClass(nomeMetodo)) != null){
				if (type instanceof Method) {
					Method metodo  = (Method)type;
					error("Method " + metodo.getId() + "is being redeclared");
				}
				
				if (type instanceof Field) {
					Field variavel = (Field)type;
					error("Method " /*+ variavel.getIdList().get(0)*/ + " has name equal to an instance variable");
				}
			}
			
			// Verifica se o metodo 'run' da classe 'Program' não é private
			if (currentClass.equals("Program") && nomeMetodo.equals("run") && qualifiers.contains(Token.PRIVATE))
				error("Method 'run' of class 'Program' cannot be private");
				

			// unary method
			lexer.nextToken();
		}
		
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		
		// Leitura qualifiers
		for (Token qualify : qualifiers) {
			if (qualify == Token.PUBLIC)
				qualifierEncapsulation = Token.PUBLIC;
				
			if (qualify == Token.PRIVATE)
				qualifierEncapsulation = Token.PRIVATE;
				
			if (qualify == Token.OVERRIDE)
				qualifierOverride = Token.OVERRIDE;
			
			if (qualify == Token.FINAL)
				qualifierFinal = Token.FINAL;
		}	
		
		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			if (currentClass.equals("Program") && nomeMetodo.equals("run"))
				error("Method 'run' of class 'Program' with a return value type");
			
			lexer.nextToken();
			returnRequiredFlag = true;
			tipo = type();
		}
		
		novo = new Method(parametros, tipo, nomeMetodo, qualifierEncapsulation, qualifierOverride, qualifierFinal);
		symbolTable.putInLocalClass(novo.getId(), novo);
		
		// Verifica se o método está sendo sobreescrico
		Object type;
		if((type = symbolTable.getInGlobal(currentClass)) == null)
			error("Classe atual nao existe");
		
		CianetoClass novaClasse = (CianetoClass)type;
		
		if (existeMetodoClasseSuperClasses(novaClasse, nomeMetodo)) {
			if(novo.getQualifierOverride() != Token.OVERRIDE)
				error("'override' expected before overridden method");
		}
		
		
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		next();
		statementList();
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}
		
		if ( returnRequiredFlag && countReturns == 0 ) {
			error("missing 'return' statement");
		}
		
		countReturns = 0;
		returnRequiredFlag = false;
		
		next();
		
		symbolTable.removeLocalIdent();
		metodoAtual = null;
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
			countWhile++;
			whileStat();
			countWhile--;
			checkSemiColon = false;
			break;
		case RETURN:
			//this.returnRequiredFlag = false;
			if(!returnRequiredFlag)
				error("Illegal 'return' statement. Method returns 'void'");
				
			returnStat();
			//checkSemiColon = false;
			break;
		case BREAK:
			// Caso não esteja em nenhum while é exibida mensagem de erro
			if ((this.countWhile+this.countRepeat) == 0)
				error("'break' statement found outside a 'while' or 'repeat' statement");
			breakStat();
			break;
		case SEMICOLON:
			checkSemiColon = false;
			next();
			break;
		case REPEAT:
			countRepeat++;
			repeatStat();
			countRepeat--;
			break;
		case VAR:
			localDec();
			break;
		case ASSERT:
			assertStat();
			break;
		default:
			
			
			
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("Out") ) {
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
		ArrayList<String> aux = new ArrayList<String>();
		Type tipo = null;
		String nomeVariavel = null;
		
		tipo = type();
		check(Token.ID, "A variable name was expected and get " + lexer.token);
		while ( lexer.token == Token.ID ) {
			nomeVariavel = lexer.getStringValue();
			
			// Verifica se a variável local está sendo redeclarada
			Object type;
			if ((type = symbolTable.getInLocal(nomeVariavel)) != null) {
				error("Variable " + nomeVariavel + " is being redeclared");
            }
			aux.add(nomeVariavel);
			symbolTable.putInLocal(nomeVariavel, new LocalDec(null, tipo, aux));
			aux.clear();
				
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
		countReturns++;
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
		next();
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
		String nameVar;
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
			case SELF:

			next();
					
			if(lexer.token == Token.DOT) {
				next();
				if(lexer.token == Token.ID) {
					nameVar = lexer.getStringValue();
					if(symbolTable.getInLocalClass(nameVar) != null) {
						//error("Variable '"+nameVar+"' was not declared");
					} else {
						Object type;
						if((type = symbolTable.getInGlobal(currentClass)) == null) {
							error("Class atual não existe");
						}
						
						CianetoClass classe = (CianetoClass)type;
						ArrayList<Method> listaMetodosPrivados = classe.getPrivateMethodList();
						ArrayList<Method> listaMetodosPublicos = classe.getPublicMethodList();
						
						boolean existeMetodo = false;
						
						if(listaMetodosPrivados != null) {
							for (Method metodo : listaMetodosPrivados) {
								if(metodo.getId().equals(nameVar)) {
									existeMetodo = true;
									break;
								}
							}
						}
						
						if(listaMetodosPublicos != null) {
							for (Method metodo : listaMetodosPublicos) {
								if(metodo.getId().equals(nameVar)) {
									existeMetodo = true;
									break;
								}
							}
						}
						
						if(!existeMetodo && !existeMetodoClasseSuperClasses(classe, nameVar))
							error("Method " + nameVar + " was not found in class or its superclasses");
					
					}
					next();
					if(lexer.token == Token.DOT) {
						next();
						if(lexer.token == Token.ID) {
							nameVar = lexer.getStringValue();
							if(symbolTable.get(nameVar) == null) {
								error("Variable '"+nameVar+"' was not declared");
							}
							next();
						}else if(lexer.token == Token.IDCOLON) {
							nameVar = lexer.getStringValue();
							nameVar = nameVar.substring(0, nameVar.length() - 1);
							if(symbolTable.get(nameVar) == null) {
								error("Variable '"+nameVar+"' was not declared");
							}
							next();
							expressionList();
						}else {
							error("identifier or identifeir colon was expected and get " +lexer.token);
						}
					}
							
					}else if(lexer.token == Token.IDCOLON) {
						nameVar = lexer.getStringValue();
						nameVar = nameVar.substring(0, nameVar.length() - 1);
						/*if(symbolTable.get(nameVar) == null) {
							error("Variable '"+nameVar+"' was not declared");
						}*/
						
						if(symbolTable.getInLocalClass(nameVar) != null) {
							//error("Variable '"+nameVar+"' was not declared");
						} else {
							Object type;
							if((type = symbolTable.getInGlobal(currentClass)) == null) {
								error("Class atual não existe");
							}
							
							CianetoClass classe = (CianetoClass)type;
							ArrayList<Method> listaMetodosPrivados = classe.getPrivateMethodList();
							ArrayList<Method> listaMetodosPublicos = classe.getPublicMethodList();
							
							boolean existeMetodo = false;
							
							if(listaMetodosPrivados != null) {
								for (Method metodo : listaMetodosPrivados) {
									if(metodo.getId().equals(nameVar)) {
										existeMetodo = true;
										break;
									}
								}
							}
							
							if(listaMetodosPublicos != null) {
								for (Method metodo : listaMetodosPublicos) {
									if(metodo.getId().equals(nameVar)) {
										existeMetodo = true;
										break;
									}
								}
							}
							
							if(!existeMetodo && !existeMetodoClasseSuperClasses(classe, nameVar))
								error("Method " + nameVar + " was not found in class or its superclasses");
						
						}
						next();
						expressionList();
					}
						
				}
					
					break;
			case SUPER:
			case ID:
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
				}else if(lexer.getStringValue().equals("super")) {
					next();
					if(lexer.token == Token.DOT) {
						next();
						if(lexer.token == Token.ID) {
							nameVar = lexer.getStringValue();
							//nameVar = nameVar.substring(0,nameVar.length() - 1);
							Object type;
							if((type = symbolTable.getInGlobal(currentClass)) == null) {
								error("Classe atual não existe");
							}
							
							CianetoClass novaClasse = (CianetoClass)type;
							
							if(!existeMetodoClasseSuperClasses(novaClasse, nameVar))
								error("Method '" + nameVar + "' was not found in its superclasses");
							
							next();
							
							
							
							break;
						}else if(lexer.token == Token.IDCOLON) {
							nameVar = lexer.getStringValue();
							nameVar = nameVar.substring(0,nameVar.length() - 1);
							/*if(symbolTable.get(nameVar) == null) {
								error("Variable '"+nameVar+"' was not declared");
							}*/
							Object type;
							if((type = symbolTable.getInGlobal(currentClass)) == null) {
								error("Classe atual não existe");
							}
							
							CianetoClass novaClasse = (CianetoClass)type;
							
							if(!existeMetodoClasseSuperClasses(novaClasse, nameVar))
								error("Method '" + nameVar + "' was not found in its superclasses");
							
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
				nameVar = lexer.getStringValue();
				Object type;
				if((type = symbolTable.getInLocal(nameVar)) == null) {
					if ((type = symbolTable.getInGlobal(nameVar)) == null) {
						error("Variable '"+nameVar+"' was not declared");
					}
				}
				
				next();
				if(lexer.token == Token.DOT) {
					next();
					
					if(lexer.getStringValue().equals("new:")) {
						error("')' expected");	
					}
					
					if(lexer.token == Token.NEW) {
						if(!(type instanceof CianetoClass))
							error("Type doesn't accept constructor");
						
						next();
						break;
					}else if(lexer.token == Token.ID) {
						nameVar = lexer.getStringValue();
						
						// Verifica se é uma variável local e caso seja é verificado se o tipo é de alguma classe
						/*if(nameVar.equals(metodoAtual)) {
							
						} else */
						
						if (type instanceof Field) {
							Field parametro = (Field)type;
							String nomeTipo = parametro.getCName();
							Object typeParametro;
							if((typeParametro = symbolTable.getInGlobal(nomeTipo)) == null) {
								error("Message send to a non-object receiver");
							}
							
							// Verifica se o método existe na classe ou nas classes mães
							CianetoClass classeVariavel = (CianetoClass)typeParametro;
							boolean existeMetodo = false;
							
							ArrayList<Method> metodosPublicos =  classeVariavel.getPublicMethodList();
							
							if (metodosPublicos != null) {
								for (Method metodo : metodosPublicos) {
									if(metodo.getId().equals(nameVar)) {
										existeMetodo = true;
										break;
									}
								}
							}
							
							if(!existeMetodo && !existeMetodoClasseSuperClasses(classeVariavel, nameVar))
								error("Method " + nameVar + " not found");
							
						}else if (!(type instanceof LocalDec)) {
							error("Message send to a non-object receiver");
						}else {
							LocalDec variavelLocal = (LocalDec)type;
							String nomeTipo = variavelLocal.getCName();
							Object typeLocalDec;
							if((typeLocalDec = symbolTable.getInGlobal(nomeTipo)) == null) {
								error("Message send to a non-object receiver");
							}
							
							// Verifica se o método existe na classe ou nas classes mães
							CianetoClass classeVariavel = (CianetoClass)typeLocalDec;
							boolean existeMetodo = true;
							
							ArrayList<Method> metodosPublicos =  classeVariavel.getPublicMethodList();
							if (metodosPublicos != null) {
								for (Method metodo : metodosPublicos) {
									if(metodo.getId().equals(nameVar)) {
										existeMetodo = true;
										break;
									}
								}
							}
							
							if(!existeMetodo && !existeMetodoClasseSuperClasses(classeVariavel, nameVar))
								error("Method " + nameVar + " not found");
						}
						
						// Procurar primeiro na hash local e depois na hash da classe
						/*if(symbolTable.getInLocal(nameVar) == null) {
							if(symbolTable.getInLocalClass(nameVar) == null) {
								error("Variable '"+nameVar+"' was not declared");
							}
						}*/
						next();
						break;
					}else if(lexer.token == Token.IDCOLON) {
						nameVar = lexer.getStringValue();
						nameVar = nameVar.substring(0,nameVar.length() - 1);
						
						if(nameVar.equals(metodoAtual)) {
							
						} else if(type instanceof Field) {
							Field parametro = (Field)type;
							String nomeTipo = parametro.getCName();
							Object typeParametro;
							if((typeParametro = symbolTable.getInGlobal(nomeTipo)) == null) {
								error("Message send to a non-object receiver");
							}
							
							// Verifica se o método existe na classe ou nas classes mães
							CianetoClass classeVariavel = (CianetoClass)typeParametro;
							boolean existeMetodo = false;
							
							ArrayList<Method> metodosPublicos =  classeVariavel.getPublicMethodList();
							
							if (metodosPublicos != null) {
								for (Method metodo : metodosPublicos) {
									if(metodo.getId().equals(nameVar)) {
										existeMetodo = true;
										break;
									}
								}
							}
							
							if(!existeMetodo && !existeMetodoClasseSuperClasses(classeVariavel, nameVar))
								error("Method " + nameVar + " not found");
							
							
						} else if (!(type instanceof LocalDec)) {
							error("Message send to a non-object receiver");
						} else {
							LocalDec variavelLocal = (LocalDec)type;
							String nomeTipo = variavelLocal.getCName();
							Object typeLocalDec;
							if((typeLocalDec = symbolTable.getInGlobal(nomeTipo)) == null) {
								error("Message send to a non-object receiver");
							}
							
							// Verifica se o método existe na classe ou nas classes mães
							CianetoClass classeVariavel = (CianetoClass)typeLocalDec;
							boolean existeMetodo = false;
							
							ArrayList<Method> metodosPublicos =  classeVariavel.getPublicMethodList();
							
							if (metodosPublicos != null) {
								for (Method metodo : metodosPublicos) {
									if(metodo.getId().equals(nameVar)) {
										existeMetodo = true;
										break;
									}
								}
							}
							
							if(!existeMetodo && !existeMetodoClasseSuperClasses(classeVariavel, nameVar))
								error("Method " + nameVar + " not found");
						}
						
						
						/*if((type2 = symbolTable.getInLocalClass(nameVar)) == null) {
							error("Variable '"+nameVar+"' was not declared");
						}
						
						
						if(!(type2 instanceof Method)) {
							error("Variable '"+nameVar+"' was not declared");
						}*/
						
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
		parametros.add(paramDec(new ArrayList<String>()));
		while(lexer.token == Token.COMMA){
			next();
			parametros.add(paramDec(new ArrayList<String>()));
		}
		
		return parametros;
	}
	private ParamDec paramDec(ArrayList<String> aux) {
		
		Type tipo = type();
		String identificador = null;
		
		if(lexer.token == Token.ID) {
			identificador = lexer.getStringValue();
			aux.add(identificador);
			symbolTable.putInLocal(identificador, new Field(Token.PRIVATE,tipo,aux));
			next();
		}else {
			error("A parameter name was expected and get " +lexer.token);
		}
		
		return new ParamDec(tipo, identificador);
	}
	private Field fieldDec(ArrayList<Token> qualifiers) {
		boolean flagVirgula = false;
		ArrayList<String> aux = new ArrayList<String>();
		lexer.nextToken(); 
		Type tipo = type();
		ArrayList<String> idList = new ArrayList<String>();
		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected and get " +lexer.token);
		}
		else {
			while ( lexer.token == Token.ID ) {
				flagVirgula = false;
				String nomeAtributo = lexer.getStringValue(); 
				
				// Verifica se a variavel se existe algum objeto com o mesmo nome na hash local da classe
				if (symbolTable.getInLocalClass(nomeAtributo) != null)
					error("Variable '" + nomeAtributo + "' is being redeclared");
				
				// Caso contrário é inserida na hash local da classe
				Token qualify = Token.PRIVATE;
				for (Token quali : qualifiers) {
					if (quali == Token.PUBLIC)
						qualify = Token.PUBLIC;
				}
				aux.add(nomeAtributo);
				symbolTable.putInLocalClass(nomeAtributo, new Field(qualify, tipo, aux));
				aux.clear();
				
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
			String nomeTipo = lexer.getStringValue();
			Object type;
			if((type = symbolTable.getInGlobal(nomeTipo)) == null) {
				error("Type " + nomeTipo + " was not found"); 
			}
			
			tipo = new CianetoClass(nomeTipo);
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
	
	private Method buscaMetodoSuperClasse(CianetoClass classe, String nomeMetodo){
		CianetoClass classeBusca = classe;  
					
		while (classeBusca.getSuperclass() != null){
			classeBusca = classeBusca.getSuperclass();
			ArrayList<Method> listaDeMetodos = classeBusca.getPublicMethodList();
			
			for (Method method : listaDeMetodos) {
				if (method.getId().equals(nomeMetodo))
					return method;
			}			
		}
		return null;
	}
	
	private boolean existeMetodoClasseSuperClasses(CianetoClass classe, String nomeMetodo){
		CianetoClass classeBusca = classe;  
		
		while (classeBusca.getSuperclass() != null){
			classeBusca = classeBusca.getSuperclass();
			ArrayList<Method> listaDeMetodos = classeBusca.getPublicMethodList();
			
			if (listaDeMetodos != null){
				for (Method method : listaDeMetodos) {
					if (method.getId().equals(nomeMetodo))
						return true;
				}			
			}
		}
		return false;
	}
	

	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;
	private boolean returnRequiredFlag; 
	private boolean programClassExists;
	private String currentClass;
	private int countWhile;
	private int countRepeat;
	private boolean metodoTemRetorno;
	private int countReturns;
	private String metodoAtual;
	//private String tipoRetorno;

}
