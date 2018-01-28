package aldor.syntax;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorDefine;
import aldor.psi.AldorId;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.components.AbstractId;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import aldor.syntax.components.SyntaxRepresentation;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AnnotatedId;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldorlib.sexpr.Symbol;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class AnnotatedSyntax {
    private static final Logger LOG = Logger.getInstance(AnnotatedSyntax.class);

    public static Syntax toSyntax(GlobalSearchScope scope, AnnotatedAbSyn ab) {
        return ReadAction.compute(() -> doToSyntax(scope, ab));
    }

    public static AnnotatedAbSyn fromSyntax(Env env, @NotNull Syntax syntax) {
        return ReadAction.compute(() -> syntax.accept(new AnnotatedAbSynSyntaxVisitor(env)));
    }

    private static Syntax doToSyntax(GlobalSearchScope scope, AnnotatedAbSyn abIn) {
        AnnotatedAbSyn ab = abIn;
        while (true) {
            if (ab.isApply()) {
                List<Syntax> all = Lists.newArrayList();
                all.add(doToSyntax(scope, ab.applyOperator()));
                all.addAll(abApplyArgs(scope, ab));
                return new Apply(all);
            } else if (ab.isId()) {
                return toSyntax(scope, ab.idSymbol());
            } else if (ab.isDeclare()) {
                return new AnnotatedAbSynDeclareNode(scope, ab);
            } else if (ab.isComma()) {
                if (ab.commaArgCount() == 0) {
                    return new Comma(Collections.emptyList());
                } else if (ab.commaArgCount() == 1) {
                    ab = ab.commaArgGet(0);
                } else {
                    return new Comma(abCommaArgs(scope, ab));
                }
            } else {
                throw new RuntimeException("Unknown syntax type: " + ab);
            }
        }
    }


    public static Syntax toSyntax(GlobalSearchScope scope, AnnotatedId id) {
        String name = id.id().name();
        Collection<AldorDefine> elts = AldorDefineTopLevelIndex.instance.get(name, scope.getProject(), scope);
        if ("Map".equals(name)) {
            return new Id(new SyntaxRepresentation<AldorIdentifier>() {
                @Nullable
                @Override
                public AldorId element() {
                    return null;
                }

                @Nullable
                @Override
                public AldorTokenType tokenType() {
                    return AldorTokenTypes.KW_RArrow;
                }

                @Override
                public String text() {
                    return "->";
                }
            });
        }
        return new Id(new SyntaxRepresentation<AldorIdentifier>() {
            @Nullable
            @Override
            public AldorIdentifier element() {
                return elts.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);
            }

            @Nullable
            @Override
            public AldorTokenType tokenType() {
                return AldorTokenTypes.TK_Id;
            }

            @Override
            public String text() {
                return name;
            }
        });
    }

    private static List<Syntax> abApplyArgs(GlobalSearchScope scope, AnnotatedAbSyn ab) {
        List<Syntax> args = new ArrayList<>(ab.applyArgCount());
        for (int i=0; i<ab.applyArgCount(); i++) {
            args.add(toSyntax(scope, ab.applyArgGet(i)));
        }
        return args;
    }


    private static List<Syntax> abCommaArgs(GlobalSearchScope scope, AnnotatedAbSyn ab) {
        List<Syntax> args = new ArrayList<>(ab.commaArgCount());
        for (int i=0; i<ab.commaArgCount(); i++) {
            args.add(toSyntax(scope, ab.commaArgGet(i)));
        }
        return args;
    }


    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    enum AnnotatedNodeType {
        Id(AldorTokenTypes.TK_Id), Declare(AldorTokenTypes.KW_Colon), Apply(null);

        @Nullable
        private final AldorTokenType tokenType;

        AnnotatedNodeType(@Nullable AldorTokenType type) {
            this.tokenType = type;
        }

        @Contract(pure = true)
        @Nullable
        public AldorTokenType tokenType() {
            return tokenType;
        }
    }

    private static class AbSynSyntaxRepresentation extends SyntaxRepresentation<PsiElement> {
        private final AnnotatedNodeType type;
        private final AnnotatedAbSyn ab;

        public AbSynSyntaxRepresentation(AnnotatedNodeType type, AnnotatedAbSyn ab) {
            this.type = type;
            this.ab = ab;
        }

        @Nullable
        @Override
        public PsiElement element() {
            return null;
        }

        @Nullable
        @Override
        public AldorTokenType tokenType() {
            return type.tokenType();
        }

        @Override
        public String text() {
            return "<<Syntax>>";
        }
    }

    private static final class AnnotatedAbSynDeclareNode extends DeclareNode<PsiElement> {
        private final AnnotatedAbSyn ab;

        private AnnotatedAbSynDeclareNode(GlobalSearchScope scope, AnnotatedAbSyn ab) {
            super(new AbSynSyntaxRepresentation(AnnotatedNodeType.Declare, ab),
                    Arrays.asList(toSyntax(scope, ab.declareId()), toSyntax(scope, ab.declareType())));
            this.ab = ab;
        }

        @Override
        public String name() {
            return ab.declareId().id().name();
        }
    }

    private static class AnnotatedAbSynSyntaxVisitor extends SyntaxVisitor<AnnotatedAbSyn> {
        private final Env env;

        public AnnotatedAbSynSyntaxVisitor(Env env) {
            this.env = env;
        }

        @Nullable
        @Override
        public AnnotatedAbSyn visitSyntax(Syntax s) {
            throw new RuntimeException("oops: " + s);
        }

        @Nullable
        @Override
        public AnnotatedAbSyn visitAnyId(AbstractId id) {
            return AnnotatedAbSyn.newId(AnnotatedId.newAnnotatedId(env, Symbol._MINUS_(id.symbol())));
        }

        @Nullable
        @Override
        public AnnotatedAbSyn visitApply(Apply apply) {
            LOG.info("Apply: " + apply + "  " + apply.arguments());
            return AxiomInterface.newApply(apply.operator().accept(this),
                    (apply.arguments().stream()
                            .map(x -> x.accept(this))
                            .collect(Collectors.toList())));
        }

        @Override
        public AnnotatedAbSyn visitDeclaration(DeclareNode<?> node) {
            return AnnotatedAbSyn.newDeclare(AnnotatedId.newAnnotatedId(env, Symbol._MINUS_(node.lhs().name())),
                    node.rhs().accept(this));
        }
    }
}
