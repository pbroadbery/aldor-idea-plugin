package aldor.util;

import aldor.util.sexpr.SExpressionTypes;

public class SxType<T extends SExpression> {
	public static final SxType<SExpression> Any = new SxType<>("Any", SExpression.class);
	public static final SxType<SExpressionTypes.Cons> Cons = new SxType<>("Cons", SExpressionTypes.Cons.class);
	public static final SxType<SExpressionTypes.IntegerAtom> Integer = new SxType<>("Integer", SExpressionTypes.IntegerAtom.class);
	public static final SxType<SExpressionTypes.StringAtom> String = new SxType<>("String", SExpressionTypes.StringAtom.class);
	public static final SxType<SExpression> Symbol = new SxType<>("Symbol", SExpression.class);
	public static final SxType<SExpressionTypes.Nil> Nil = new SxType<>("Nil", SExpressionTypes.Nil.class);
	private final Class<T> clss;
	private final java.lang.String name;

	public SxType(String typeName, Class<T> class1) {
		this.name = typeName;
		this.clss = class1;
	}

	@Override
	public java.lang.String toString() {
		return this.name;
	}

	public T cast(SExpression sx) {
		if (sx.isOfType(this)) {
			return this.clss.cast(sx);
		}
		throw new IllegalArgumentException("Cast wanted a " + this + " got: " + sx.type());
	}



}