package aldor.syntax;


import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Define;
import aldor.syntax.components.Id;
import aldor.syntax.components.Other;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Syntax typeName(Syntax syntax) {
        Syntax syntax1 = syntax;
        if (syntax1.is(DeclareNode.class)) {
            syntax1 = syntax1.as(DeclareNode.class).lhs();
        }
        if (syntax1.is(Id.class)) {
            return syntax1;
        }
        if (syntax1.is(Apply.class)) {
            return new Apply(syntax1.psiElement(),
                    Stream.concat(Stream.of(typeName(syntax1.as(Apply.class).operator())),
                            syntax1.as(Apply.class).arguments().stream().map(SyntaxUtils::definingId)).collect(Collectors.toList()));
        }
        return new Other(syntax1.psiElement());
    }

    private static Syntax definingId(Syntax item) {
        Syntax definingId = item;
        if (definingId.is(Define.class)) {
            definingId = definingId.as(Define.class).lhs();
        }
        if (definingId.is(DeclareNode.class)) {
            definingId = definingId.as(DeclareNode.class).lhs();
        }
        if (definingId.is(Id.class)) {
            return definingId;
        }
        return new Other(definingId.psiElement());
    }

}
