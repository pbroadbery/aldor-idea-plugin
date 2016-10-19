package aldor.syntax.components;

import aldor.syntax.Syntax;
import aldor.util.SExpression;
import org.jetbrains.annotations.Nullable;

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
        return "Other";
    }

    @Override
    @Nullable
    public Iterable<Syntax> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "{?:" + name() + ": " + sx + "}";
    }

}
