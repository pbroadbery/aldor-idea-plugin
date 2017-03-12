package aldor.syntax.components;


import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SyntaxUtils {

    public static Iterable<Syntax> childScopesForDefineLhs(@NotNull Syntax syntax) {
        Syntax maybeLhs = syntax;
        if (maybeLhs.is(DeclareNode.class)) {
            maybeLhs = maybeLhs.as(DeclareNode.class).lhs();
        }
        Collection<Syntax> scopes = new ArrayList<>();
        if (maybeLhs.is(Id.class)) {
            scopes.add(maybeLhs);
        }
        if (maybeLhs.is(Apply.class)) {
            Apply apply = maybeLhs.as(Apply.class);
            List<Syntax> ll = apply.arguments();
            for (Syntax child : ll) {
                if (child.is(Comma.class)) {
                    for (Syntax commaElt : child.children()) {
                        scopes.add(commaElt);
                    }
                }
                else if (child.is(DeclareNode.class)) {
                    scopes.add(child);
                }
                else if (child.is(Id.class)) {
                    scopes.add(child);
                }
            }
        }
        return scopes;
    }

    public static Iterable<Syntax> childScopesForLambdaLhs(@NotNull Syntax syntax) {
        Syntax inner = syntax;
        if (inner.is(DeclareNode.class)) {
            inner = inner.as(DeclareNode.class).lhs();
        }

        if (inner.is(Comma.class)) {
            return inner.as(Comma.class).children();
        }
        return Collections.singleton(inner);
    }

    public static Optional<Syntax> childScopeForMacroLhs(Syntax syntax) {
        Syntax leftmost = syntax;
        while (leftmost.is(Apply.class)) {
            leftmost = leftmost.as(Apply.class).operator();
        }
        if (leftmost.is(Id.class)) {
            return Optional.of(leftmost);
        }
        return Optional.empty();
    }
}
