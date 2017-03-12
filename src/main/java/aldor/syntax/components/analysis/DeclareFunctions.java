package aldor.syntax.components.analysis;

import aldor.syntax.Syntax;
import aldor.syntax.components.AbstractId;
import aldor.syntax.components.Apply;

import java.util.Optional;

public final class DeclareFunctions {

    public static Optional<AbstractId> declareId(Syntax lhsSyntax) {
        Syntax syntax = lhsSyntax;
        while (syntax.is(Apply.class)) {
            syntax = syntax.as(Apply.class).operator();
        }
        if (syntax.is(AbstractId.class)) {
            return Optional.of(syntax.as(AbstractId.class));
        }
        return Optional.empty();
    }

}
