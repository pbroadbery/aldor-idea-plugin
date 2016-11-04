package aldor.syntax.components;


import aldor.syntax.Syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SyntaxUtils {

    public static Iterable<Syntax> childScopesForDefineLhs(Syntax syntax) {
        if ((syntax == null) || !syntax.is(Declaration.class)) {
            return Collections.singleton(syntax);
        }
        Declaration decl = syntax.as(Declaration.class);
        assert decl != null;
        Collection<Syntax> scopes = new ArrayList<>();
        if (decl.lhs().is(Apply.class)) {
            Apply apply = decl.lhs().as(Apply.class);
            assert apply != null;
            List<Syntax> ll = apply.arguments();
            for (Syntax child : ll) {
                if (child.as(Comma.class) != null) {
                    for (Syntax commaElt: child.children()) {
                        scopes.add(commaElt);
                    }
                }
                else if (child.as(Declaration.class) != null){
                    scopes.add(child);
                }

            }
        }
        return scopes;
    }

}
