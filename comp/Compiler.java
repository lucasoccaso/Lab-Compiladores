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
			catch ( RuntimeException e ) {
				e.printStackTrace();
				thereWasAnError = true;
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
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				error("Annotation 'cep' takes three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  ) {
				error("The first parameter of annotation 'cep' should be an integer number");
			}
			else {
				int ln = (Integer ) metaobjectParamList.get(0);
				metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				error("The second and third parameters of annotation 'cep' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )
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

	// AssertStat ::= “assert” Expression “,” StringValue
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

	// AssignExpr ::= Expression [ “=” Expression ]
	private void assignExpr() {
        next();
        expr();
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
	

	private void breakStat() {
		next();

	}

	private void returnStat() {
		next();
		expr();
	}


	// ClassDec ::= [ “open” ] “class” Id [ “extends” Id ] MemberList “end”
	private void classDec() {
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			// open class
		}
		if ( lexer.token != Token.CLASS ) error("'class' expected");
		lexer.nextToken();
		if ( lexer.token != Token.ID )
			error("Identifier expected");
		String className = lexer.getStringValue();
		lexer.nextToken();
		if ( lexer.token == Token.EXTENDS ) {
			lexer.nextToken();
			if ( lexer.token != Token.ID )
				error("Identifier expected");
			String superclassName = lexer.getStringValue();

			lexer.nextToken();
		}

		memberList();
		if ( lexer.token != Token.END)
			error("'end' expected");
		lexer.nextToken();

	}

	// CompStatement ::= “{” { Statement } “}”
	private void compStat(){
    	check(Token.LEFTCURBRACKET, "left curl bracket expected.");
    	next();
    	while(lexer.toke != Token.RIGHTCURBRACKET){
    		statement();
    	}
    	check(Token.LEFTCURBRACKET, "left curl bracket expected.");
    	next();
    }

    // Digit ::= “0” | ... | “9”
    private void digit(){

    }

    // Expression ::= SimpleExpression [ Relation SimpleExpression ]
	private void expr() {
		try {
            ArrayList<Type> t1 = null;
            ArrayList<Type> t2 = null;
            t1 = simpleExpression();
            if (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.LE
                    || lexer.token == Token.GT || lexer.token == Token.GE || lexer.token == Token.NEQ) {
                next();
                ret = new TypeBoolean();
                t2 = simpleExpression();
                arrayTypes.addAll(t2);
            }

            arrayTypes.addAll(t1);
        } catch (NullPointerException e) {

        }
	}

	// ExpressionList ::= Expression { “,” Expression }
	private void expressionList() {
        expr();

        while (lexer.token == Token.COMMA) {
            next();
            expr();
        }
    }

    /*
    Factor ::= BasicValue |
	“(” Expression “)” |
	“!” Factor |
	“nil” |
	ObjectCreation |
	PrimaryExpr
    */
    private void factor(){

    	if(lexer.token == Token.LITERALINT || lexer.token == Token.TRUE 
    		lexer.token == Token.FALSE || lexer.token == Token.LITERALSTRING){
    		next();
    	} else if(lexer.token == Token.LEFTPAR){
    		next();
    		expr();
    		check(Token.RIGHTPAR, ") expected");
            next();
    	} else if(lexer.token == Token.NOT){
    		next();
    		factor();
    	} else if(lexer.token == Token.NIL){
    		next();
    	} else if(lexer.token == Token.ID){

    		//Não sei oq fazer ainda
    	} else if(lexer.token == Token.SUPER || lexer.token == Token.SELF){
    		primaryExpr();
    	} else if(lexer.token == Token.IN){
    		readExpr();
    	}
    }

    // FieldDec ::= “var” Type IdList [ “;” ]
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

	// FormalParamDec ::= ParamDec { “,” ParamDec }
	private void formalParamDec() {
        paramDec();

        while (lexer.token == Token.COMMA) {
            next();
            paramDec();
        }
    }

    // HighOperator ::= “∗” | “/” | “&&”
    private boolean highOperator(){
    	if (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
            return true;
        }

        return false;
    }

    // IfStat ::= “if” Expression “{” Statement “}” [ “else” “{” Statement “}” ]
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

    // IntValue ::= Digit { Digit }
    private void intValue() {
        next();
    }

    // LocalDec ::= “var” Type IdList [ “=” Expression ]
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

	// LowOperator ::= “+” | “−” | “||”
	private boolean lowOperator() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
            return true;
        }

        return false;
    }

    // MemberList ::= { [ Qualifier ] Member }
    private void memberList() {
		while ( true ) {
			qualifier();
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

	/* MethodDec ::= “func” IdColon FormalParamDec [ “->” Type ]
	“{” StatementList “}” |
	“func” Id [ “->” Type ] “{” StatementList “}”
	*/
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

	// ParamDec ::= Type Id
	private void paramDec(String type) {

        // Se nao encontrar um identificador apos o tipo de uma variavel, lanca um erro
        if (lexer.token != Token.ID) {
            error("A variable name was expected");
        }

        next(); // Consome o id
    }

	/*Qualifier ::= “private”
	“public”
	“override”
	“override” “public”
	“final”
	“final” “public”
	“final” “override”
	“final” “override” “public”
	“shared” “private”
	“shared” “public”
	*/
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

	// readExpr ::= 'In' '.' [ 'readInt' | 'readString' ]
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

    // RepeatStat ::= “repeat” StatementList “until” Expression
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

	/*PrimaryExpr ::= “super” “.” IdColon ExpressionList |
	“super” “.” Id |
	Id |
	Id “.” Id |
	Id “.” IdColon ExpressionList |
	“self” |
	“self” “.” Id |
	“self” ”.” IdColon ExpressionList |
	“self” ”.” Id “.” IdColon ExpressionList |
	“self” ”.” Id “.” Id |
	ReadExpr
	*/
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

	// Relation ::= “==” | “<” | “>” | “<=” | “>=” | “! =”
	private boolean relation() {
        if (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT ||
                lexer.token == Token.LE || lexer.token == Token.GE || lexer.token == Token.NEQ) {
            return true;
        }

        return false;
    }

	// Signal ::= “+” | “−”
	private boolean signal() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
            return true;
        }

        return false;
    }

    // SignalFactor ::= [ Signal ] Factor
    private Expr signalFactor(){

    	if(signal()){
    		next();
    	}

    	return Factor();
    }

    // SimpleExpression ::= SumSubExpression { “++” SumSubExpression }
    private void SumSubExpr(){

    }

   	// SumSubExpression ::= Term { LowOperator Term }
    private void sumSubExpr(){

    }

    /*Statement ::= AssignExpr “;” | IfStat | WhileStat | ReturnStat “;” |
	WriteStat “;” | “break” “;” | “;” |
	RepeatStat “;” | LocalDec “;” |
	AssertStat “;”
    */
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

	// StatementList ::= { Statement }
	private void statementList() {
		  // only '}' is necessary in this test
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
			statement();
		}
	}

    //Term ::= SignalFactor { HighOperator SignalFactor }
    private Expr term(){
    	Expr first = signalFactor();

    	while(highOperator()){
    		next();
    		Expr second = signalFactor();
    	}

    	return first;
    }

    // Type ::= BasicType | Id
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

	// WriteStat ::= “Out” “.” [ “print:” | “println:” ] Expression
	private void writeStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.getStringValue();
		expr();
	}

	// WhileStat ::= “while” Expression “{” StatementList “}”
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
