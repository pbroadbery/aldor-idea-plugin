package aldor.util.sexpr.impl;

public class WhitespaceFilter implements ITokeniser {
	private final ITokeniser underlying;

	WhitespaceFilter(ITokeniser underlying) {
		this.underlying = underlying;
	}

	@Override
	public Token peek() {
		Token tok = underlying.peek();
		while (tok.isWhitespace()) {
			underlying.next();
			tok = underlying.peek();
		}
		assert (!tok.isWhitespace());
		return tok;
	}

	@Override
	public void next() {
		underlying.next();
	}

	@Override
	public boolean hasNext() {
		return underlying.hasNext();
	}

}
