package aldor.syntax.components;


import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SyntaxUtils {

    public static Iterable<Syntax> childScopesForDefineLhs(@NotNull Syntax syntax) {
        if (syntax.is(DeclareNode.class)) {
            syntax = syntax.as(DeclareNode.class).lhs();
        }
        Collection<Syntax> scopes = new ArrayList<>();
        if (syntax.is(Apply.class)) {
            Apply apply = syntax.as(Apply.class);
            List<Syntax> ll = apply.arguments();
            for (Syntax child : ll) {
                if (child.is(Comma.class)) {
                    for (Syntax commaElt: child.children()) {
                        scopes.add(commaElt);
                    }
                }
                else if (child.is(DeclareNode.class)){
                    scopes.add(child);
                }

            }
        }
        return scopes;
    }

}
