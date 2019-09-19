// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package lexer;

public enum Token {

    ASSERT("assert"),
    BOOLEAN("Boolean"),
    BREAK("break"),
    CLASS("class"),
    ELSE("else"),
    EXTENDS("extends"),
    FALSE("false"),
    FINAL("final"),
    FUNC("func"),
    IF("if"),
    INT("Int"),
    NIL("nil"),
    OVERRIDE("override"),
    PRIVATE("private"),
    PUBLIC("public"),
    REPEAT("repeat"),
    RETURN("return"),
    SELF("self"),
    STRING("String"),
    SUPER("super"),
    TRUE("true"),
    UNTIL("until"),
    VAR("var"),
    WHILE("while"),

	Token(String name) {
		this.name = name;
	}

	@Override public String toString() {
		return name;
	}
	private String name;
}