// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import ast.LiteralInt;
import ast.MetaobjectAnnotation;
import ast.Program;
import ast.Statement;
import ast.TypeCianetoClass;
import lexer.Lexer;
import lexer.Token;
import ast.*;


public class Compiler {

	public Compiler() { }

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
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		ArrayList<TypeCianetoClass> CianetoClassList = new ArrayList<>();
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
			catch ( Throwable e ) {
	            e.printStackTrace();
	            thereWasAnError = true;
	            // adicione as linhas abaixo
	            try {
	                error("Exception '" + e.getClass().getName() + "' was thrown and not caught. "
	                        + "Its message is '" + e.getMessage() + "'");
	            }
	            catch( CompilerError ee) {
	            }
	            return program; // add this line
	        }

		}
		if ( !thereWasAnError && lexer.token != Token.EOF ) {
			try {
				error("End of file expected");
			}
			catch( CompilerError e) {
			}
		}
		return program;
	}

	/**  parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     * {@literal @}cep(5, "'class' expected") <br>
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
				error("')' expected after annotation with parameters");
			else {
				getNextToken = true;
			}
		}
		switch ( name ) {
		case "nce":
			if ( metaobjectParamList.size() != 0 )
				error("Annotation 'nce' does not take parameters");
			break;
		case "cep":
			int sizeParamList = metaobjectParamList.size();
			if ( sizeParamList < 2 || sizeParamList > 4 )
			error("Annotation 'cep' takes two, three, or four parameters");

			if ( !( metaobjectParamList.get(0) instanceof Integer) ) {
			error("The first parameter of annotation 'cep' should be an integer number");
			}
			else {
			int ln = (Integer ) metaobjectParamList.get(0);
			metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) )
			error("The second parameter of annotation 'cep' should be a literal string");
			if ( sizeParamList >= 3 && !( metaobjectParamList.get(2) instanceof String) )
			error("The third parameter of annotation 'cep' should be a literal string");
			if ( sizeParamList >= 4 && !( metaobjectParamList.get(3) instanceof String) )
			error("The fourth parameter of annotation 'cep' should be a literal string");

			break;
		case "annot":
			if ( metaobjectParamList.size() < 2  ) {
				error("Annotation 'annot' takes at least two parameters");
			}
			for ( Object p : metaobjectParamList ) {
				if ( !(p instanceof String) ) {
					error("Annotation 'annot' takes only String parameters");
				}
			}
			if ( ! ((String ) metaobjectParamList.get(0)).equalsIgnoreCase("check") )  {
				error("Annotation 'annot' should have \"check\" as its first parameter");
			}
			break;
		default:
			error("Annotation '" + name + "' is illegal");
		}
		metaobjectAnnotationList.add(new MetaobjectAnnotation(name, metaobjectParamList));
		if ( getNextToken ) lexer.nextToken();
	}

	public Statement assertStat() {
		Expr expr;
		
		lexer.nextToken();
		
		expr = expr();
		if ( lexer.token != Token.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( lexer.token != Token.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String literalString = lexer.getLiteralStringValue();
		lexer.nextToken();
		if ( lexer.token == Token.SEMICOLON )
			lexer.nextToken();

		return new AssertStat(expr, literalString);
	}

	// AssignExpr ::= Expression [ "=" Expression ]
	private AssignExpr assignExpr() {
		Expr left, right = null;
		
		left = expr();
        next();
        if( lexer.token == Token.ASSIGN ) {
        	right = expr();
        }

        return new AssignExpr(left, right);
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
	

	// Apenas lê o 'break'
	private void breakStat() {
		next();
	}


	private Expr returnStat() {

		Expr expr = simpleExpr();

		if ( lexer.token != Token.RETURN) {
			error("'return' was expected");
		}
		next();
		
		expr = expr();

		return expr;
	}


	// ClassDec ::= [ "open" ] "class" Id [ "extends" Id ] MemberList "end"
	private TypeCianetoClass classDec() {
		MemberList memberlist = null;
		
		// Verifica se começa com "open"
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			// open class
			lexer.nextToken();
		}
		// Verifica se tem "class"
		if ( lexer.token != Token.CLASS ) {
			error("'class' was expected");
		}
		
		lexer.nextToken();
		//Verifica se tem um Id
		if ( lexer.token != Token.ID ) {
			error("Identifier was expected");
		}
		
		// Guarda o nome da classe
		String className = lexer.getStringValue();
		
		//VERIFICAR SE A CLASSE JA EXISTE
		
		lexer.nextToken();
		// Verifica se a classe extende de alguma superclasse
		if ( lexer.token == Token.EXTENDS ) {
			lexer.nextToken();
			
			// Verifica sem tem um Id da superclasse
			if ( lexer.token != Token.ID ) {
				error("Identifier was expected");
			}
			String superclassName = lexer.getStringValue();

			// VERIFICAR SE A SUPERCLASSE EXISTE
			// VERIFICAR SE PODE EXTENDER DESSA SUPERCLASSE
			
			lexer.nextToken();
		}

		memberlist = memberList();
		if ( lexer.token != Token.END)
			error("'end' was expected");
		lexer.nextToken();

	}

	private void compStat(){
    	check(Token.LEFTCURBRACKET, "left curl bracket was expected.");
    	next();
    	while(lexer.token != Token.RIGHTCURBRACKET){
    		statement();
    	}
    	check(Token.LEFTCURBRACKET, "left curl bracket was expected.");
    	next();
    }

    private void digit(){

    }

    // Expression ::= SimpleExpression [ Relation SimpleExpression ]
	private Expr expr() {
          
        Expr left = simpleExpr();
        Token op = lexer.token;
        
        if (op == Token.EQ || op == Token.LT || op == Token.LE
                || op == Token.GT || op == Token.GE || op == Token.NEQ) {
            next();
            
            Expr right = simpleExpr();
        
            left = new CompositeExpr(left, lexer.token, right);
          
        }

       
     
		
		return left;
	}


	private void expressionList() {
        expr();

        while (lexer.token == Token.COMMA) {
            next();
            expr();
        }
    }


    private Expr factor(){

    	if(lexer.token == Token.LITERALINT || lexer.token == Token.TRUE || lexer.token == Token.FALSE || lexer.token == Token.LITERALSTRING){
    		next();
    	} else if(lexer.token == Token.LEFTPAR){
    		next();
    		expr();
    		check(Token.RIGHTPAR, ") expected");
            next();
    	} else if(lexer.token == Token.NOT){
    		next();
    		factor();
    	} else if(lexer.token == Token.NULL){
    		next();
    	} else if(lexer.token == Token.ID){


    	} else if(lexer.token == Token.SUPER || lexer.token == Token.SELF){
    		primaryExpr();
    	} else if(lexer.token == Token.IN){
    		readExpr();
    	}
    	
    	return null;
    }

	private void fieldDec() {
		lexer.nextToken();
		type();
		if ( lexer.token != Token.ID ) {
			this.error("A field name was expected");
		}
		else {
			while ( lexer.token == Token.ID  ) {
				lexer.nextToken();
				if ( lexer.token == Token.COMMA ) {
					lexer.nextToken();
				}
				else {
					break;
				}
			}
		}

	}

	private void formalParamDec() {
        paramDec();

        while (lexer.token == Token.COMMA) {
            next();
            paramDec();
        }
    }

    private boolean highOperator(){
    	if (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
            return true;
        }

        return false;
    }

	private void ifStat() {
		next();
		expr();
		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE ) {
			statement();
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected");
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				statement();
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected");
		}
	}

    private void intValue() {
        next();
    }

    private void localDec() {
		next();
		type();
		check(Token.ID, "A variable name was expected");
		while ( lexer.token == Token.ID ) {
			next();
			if ( lexer.token == Token.COMMA ) {
				next();
			}
			else {
				break;
			}
		}
		if ( lexer.token == Token.ASSIGN ) {
			next();
			// check if there is just one variable
			expr();
		}

	}

	private boolean lowOperator() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
            return true;
        }

        return false;
    }

	// MemberList ::= { [ Qualifier ] Member }
    private MemberList memberList() {
    	
		while ( true ) {
			
			String qualifier = qualifier();
			
			if ( lexer.token == Token.VAR ) {
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

    
    
	private void methodDec() {
		lexer.nextToken();
		if ( lexer.token == Token.ID ) {
			// unary method
			lexer.nextToken();

		}
		else if ( lexer.token == Token.IDCOLON ) {
			// keyword method. It has parameters

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
			error("'{' expected");
		}
		next();

	}


	private void paramDec() {

        // Se nao encontrar um identificador apos o tipo de uma variavel, lanca um erro
        if (lexer.token != Token.ID) {
            error("A variable name was expected");
        }

        next(); // Consome o id
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

    private String readExpr() {
    	String auxType = "";
        next(); 
        check(Token.DOT, "a '.' was expected after 'In'");
        next(); 
        if (lexer.token == Token.READINT){
            next();
            auxType = "int";
        } else if (lexer.token == Token.READSTRING) {
        	next();
        	auxType = "string";
        } else {   
            error("expected 'readInt' or 'readString' after '.'");
        }
        
        return auxType;
    }

    private void repeatStat() {
		next();
		while ( lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
		check(Token.UNTIL, "missing keyword 'until'");
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


    private void primaryExpr() {
        if (lexer.token == Token.SUPER) {
            next();

            check(Token.DOT, "dot expected");
            next();

            if (lexer.token == Token.IDCOLON) {
                next();
                expressionList();
            } else if (lexer.token == Token.ID) {
                next();
            } else {
				error("ID or ID colon expected");
            }
        } else if (lexer.token == Token.ID) {
            next();

            if (lexer.token == Token.DOT) {
                next();
                if (lexer.token == Token.ID) {
                	next();
            	} else if (lexer.token == Token.IDCOLON) {
                	next();
                	expressionList();
	            } else {
					error("ID or ID colon expected");
	            }
            }
        } else if (lexer.token == Token.SELF) {
            next();
            if (lexer.token == Token.DOT) {
                next();
                if (lexer.token == Token.ID) {
                    next();
                    if (lexer.token == Token.DOT) {
                        next();
						if (lexer.token == Token.ID) {
                			next();
            			} else if (lexer.token == Token.IDCOLON) {
                			next();
                			expressionList();
	            		} else {
							error("ID or ID colon expected");
	            		}                    	
                    }
                } else if(lexer.token == Token.IDCOLON){
                	next();
                	expressionList();
                }
            }
        } else if (lexer.getStringValue().equals("In")) {
            readExpr();
        }
    }

	private boolean relation() {
        if (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT ||
                lexer.token == Token.LE || lexer.token == Token.GE || lexer.token == Token.NEQ) {
            return true;
        }

        return false;
    }

	private boolean signal() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
            return true;
        }

        return false;
    }

    private Expr signalFactor(){

    	if(signal()){
    		next();
    	}

    	return factor();
    }

    
    // SimpleExpression ::= SumSubExpression { "++" SumSubExpression }
    private Expr simpleExpr() {
		Token op;

		//Pega o primeiro Term
		Expr left = term();
		//Verifica se há um LowOperator após o Term
		while ((op = lexer.token) == Token.PLUSPLUS) {
			//Consome o LoewOperator
			lexer.nextToken();
			//Le o proximo Term
			Expr right = term();
            
			left = new CompositeExpr(left, op, right);
		}
		return left;
	}

    private void SumSubExpr(){

    }

    private void sumSubExpr(){

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
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("Out") ) {
				writeStat();
			}
			else {
				expr();
			}

		}
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected");
		}
	}


	private void statementList() {
		  // only '}' is necessary in this test
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
	}


    private Expr term(){
    	Expr first = signalFactor();

    	while(highOperator()){
    		next();
    		Expr second = signalFactor();
    	}

    	return first;
    }


	private void type() {
		// BasicType ::= 'Int' | 'Boolean' | 'String'
		if ( lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING ) {
			next();
		}
		else if ( lexer.token == Token.ID ) {
			next();
		}
		else {
			this.error("A type was expected");
		}

	}


	private void writeStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.getStringValue();
		expr();
	}


	private void whileStat() {
		next();
		expr();
		check(Token.LEFTCURBRACKET, "missing '{' after the 'while' expression");
		next();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
		check(Token.RIGHTCURBRACKET, "missing '}' after 'while' body");
	}


	private static boolean startExpr(Token token) {

		return token == Token.FALSE 			|| token == Token.TRUE
				|| token == Token.NOT 			|| token == Token.SELF
				|| token == Token.LITERALINT 	|| token == Token.SUPER
				|| token == Token.LEFTPAR		|| token == Token.NULL
				|| token == Token.ID 			|| token == Token.LITERALSTRING;

	}

	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
