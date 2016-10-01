package aldor.util.sexpr;


public final class Token {
	private final TokenType type;
	private final String text;

	public Token(TokenType type, String text) {
		this.type = type;
		this.text = text;
	}

	public final String text() {
		return text;
	}

	public final TokenType type() {
		return type;
	}

	@Override
	public final String toString() {
		return "{T:" + type +" " + text + "}";
	}

	public boolean isWhitespace() {
		return type.isWhitespace();
	}

}
