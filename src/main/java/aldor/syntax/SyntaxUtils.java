package aldor.syntax;


import aldor.lexer.AldorTokenTypes;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Define;
import aldor.syntax.components.Id;
import aldor.syntax.components.Literal;
import aldor.syntax.components.Other;
import com.google.common.collect.Streams;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SyntaxUtils {

    /* a: X --> {a: X},
     * a(x: X, y: Y): Z == { x: X, y: Y }
     */
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
                    scopes.addAll(child.children());
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

    /**
     * Find element on given syntactic form
     * @param syntax Some syntax - generally a constructor form
     * @return psi representing the definition of this element
     */
    @Nullable
    public static PsiElement psiElementFromSyntax(Syntax syntax) {
        Syntax syntax1 = syntax;
        while (true) {
            if (syntax1.psiElement() != null) {
                return syntax1.psiElement();
            }
            if (syntax1.is(Apply.class)) {
                syntax1 = syntax1.as(Apply.class).operator();
            }
            else {
                return null;
            }
        }
    }

    /**
     * Foo(X: A) -> Foo(X)
     * @param syntax a type form
     * @return the name of the type
     */
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

    /**
     * foo(x: A, y: B): X --> (x: A, y: B) -> X
     * A: X -> X
     */
    private static final Id mapsTo = Id.createMissingId(AldorTokenTypes.KW_RArrow, "->");

    public static Optional<Syntax> definitionToSignature(Syntax syntax) {
        if (syntax.is(DeclareNode.class)) {
            DeclareNode<?> declare = syntax.as(DeclareNode.class);
            Syntax lhs = declare.lhs();
            Optional<Apply> applyMaybe = lhs.maybeAs(Apply.class);
            return applyMaybe.<Optional<Syntax>>map(apply -> Optional.of(new Apply(mapsTo,
                                         Arrays.asList(new Comma(sigParameters(apply.arguments())),
                                         declare.rhs()))))
                            .orElseGet(() -> Optional.of(declare.rhs()));
        }
        return Optional.empty();
    }

    private static List<Syntax> sigParameters(Collection<Syntax> arguments) {
        Id unknown = Id.createMissingId(AldorTokenTypes.TK_Id, "??");
        return arguments.stream().map(arg -> arg.maybeAs(DeclareNode.class).map(DeclareNode::rhs).orElse(unknown)).collect(Collectors.toList());
    }

    /**
     * Foo(A: X, B: Y) --> Foo, foo: X == bar --> foo
     * Note that if the end item is not an id, "Other(?)" is returned
     * @param item a declaration or definition
     * @return the id being defined.
     */
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

    public static Syntax leadingId(Syntax syntax) {
        Syntax leadingPart = syntax;
        while (leadingPart.is(Apply.class)) {
            leadingPart = leadingPart.as(Apply.class).operator();
        }
        return leadingPart;
    }

    public static boolean match(Syntax sourceSyntax, Syntax librarySyntax) {
        return match1(sourceSyntax, librarySyntax);
    }

    private static boolean match1(Syntax sourceSyntax, Syntax librarySyntax) {
        if (sourceSyntax.is(Id.class) && librarySyntax.is(Id.class)) {
            return sourceSyntax.as(Id.class).symbol().equals(librarySyntax.as(Id.class).symbol());
        }
        if (sourceSyntax.is(Apply.class) && librarySyntax.is(Apply.class)) {
            Apply sourceApply = sourceSyntax.as(Apply.class);
            Apply libraryApply = librarySyntax.as(Apply.class);
            return match(sourceApply.operator(), libraryApply.operator())
                    && (sourceApply.arguments().size() == libraryApply.arguments().size())
                    && Streams.zip(sourceApply.arguments().stream(), libraryApply.arguments().stream(), SyntaxUtils::match).allMatch(m -> m);
        }
        if (sourceSyntax.is(Comma.class) && librarySyntax.is(Comma.class)) {
            Comma sourceComma = sourceSyntax.as(Comma.class);
            Comma libraryComma = librarySyntax.as(Comma.class);
            if (sourceComma.children().size() != libraryComma.children().size()) {
                return false;
            }
            return  Streams.zip(sourceComma.children().stream(), libraryComma.children().stream(), SyntaxUtils::match).allMatch(m -> m);
        }
        if (sourceSyntax.is(Literal.class) && librarySyntax.is(Literal.class)) {
            return sourceSyntax.as(Literal.class).text().equals(librarySyntax.as(Literal.class).text());
        }
        return false;
    }
}
