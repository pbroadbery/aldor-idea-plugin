package aldor.util.sexpr.impl;

public enum TokenType {
	OParen, CParen, String, Integer, Symbol, DOT, NL(true), WS(true), EOF;

	private final boolean isWhitespace;

	TokenType() {
		this(false);
	}

	TokenType(boolean isWhitespace) {
		this.isWhitespace = isWhitespace;
	}

	public boolean isWhitespace() {
		return isWhitespace;
	}
}
