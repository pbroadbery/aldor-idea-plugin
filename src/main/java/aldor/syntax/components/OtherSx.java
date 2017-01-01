package aldor.syntax.components;

import aldor.syntax.Syntax;
import aldor.util.sexpr.SExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Placeholder where we can't figure out what's going on.
 */
public class OtherSx extends Other {
    private final SExpression sx;

    public OtherSx(SExpression sx) {
        super(null);
        this.sx = sx;
    }

    @Override
    public String name() {
        return "OtherSx";
    }

    @Override
    @Nullable
    public Collection<Syntax> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "{?:" + name() + ": " + sx + "}";
    }

}
