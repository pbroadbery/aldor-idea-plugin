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
import aldor.syntax.components.Literal;
import aldor.syntax.components.SyntaxRepresentation;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AnnotatedId;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldorlib.sexpr.Symbol;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
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

    public static Syntax toSyntax(Project project, GlobalSearchScope scope, AnnotatedAbSyn ab) {
        try {
            return ReadAction.compute(() -> (new ToSyntaxConverter(scope, project).doToSyntax(ab)));
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new SyntaxConversionException("Failed to convert " + ab, e);
        }
    }

    public static AnnotatedAbSyn fromSyntax(Env env, @NotNull Syntax syntax) {
        try {
            return ReadAction.compute(() -> syntax.accept(new AnnotatedAbSynSyntaxVisitor(env)));
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new SyntaxConversionException("Failed to convert " + syntax, e);
        }
    }

    private static final class ToSyntaxConverter {
        private final GlobalSearchScope scope;
        private final Project project;

        private ToSyntaxConverter(GlobalSearchScope scope, Project project) {
            this.scope = scope;
            this.project = project;
        }

        private Syntax doToSyntax(AnnotatedAbSyn abIn) {
            AnnotatedAbSyn ab = abIn;
            while (true) {
                if (ab.isApply()) {
                    List<Syntax> all = Lists.newArrayList();
                    all.add(doToSyntax(ab.applyOperator()));
                    all.addAll(abApplyArgs(ab));
                    return new Apply(all);
                } else if (ab.isId()) {
                    return toSyntax(ab.idSymbol());
                } else if (ab.isDeclare()) {
                    return new AnnotatedAbSynDeclareNode(this, ab);
                } else if (ab.isComma()) {
                    switch (ab.commaArgCount()) {
                        case 0:
                            return new Comma(Collections.emptyList());
                        case 1:
                            ab = ab.commaArgGet(0);
                            break;
                        default:
                            return new Comma(abCommaArgs(scope, ab));
                    }
                } else if (ab.isLiteral()) {
                    return new Literal(ab.literal(), null);
                } else {
                    throw new SyntaxConversionException("Unknown syntax type: " + ab);
                }
            }
        }


        public Syntax toSyntax(AnnotatedId id) {
            String name = id.id().name();
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
            Collection<AldorDefine> elts = AldorDefineTopLevelIndex.instance.get(name, project, scope);
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

        private List<Syntax> abApplyArgs(AnnotatedAbSyn ab) {
            List<Syntax> args = new ArrayList<>(ab.applyArgCount());
            for (int i = 0; i < ab.applyArgCount(); i++) {
                args.add(doToSyntax(ab.applyArgGet(i)));
            }
            return args;
        }


        private List<Syntax> abCommaArgs(GlobalSearchScope scope, AnnotatedAbSyn ab) {
            List<Syntax> args = new ArrayList<>(ab.commaArgCount());
            for (int i = 0; i < ab.commaArgCount(); i++) {
                args.add(doToSyntax(ab.commaArgGet(i)));
            }
            return args;
        }
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

        AbSynSyntaxRepresentation(AnnotatedNodeType type, AnnotatedAbSyn ab) {
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

        private AnnotatedAbSynDeclareNode(ToSyntaxConverter converter, AnnotatedAbSyn ab) {
            super(new AbSynSyntaxRepresentation(AnnotatedNodeType.Declare, ab),
                    Arrays.asList(converter.toSyntax(ab.declareId()), converter.doToSyntax(ab.declareType())));
            this.ab = ab;
        }

        @Override
        public String name() {
            return ab.declareId().id().name();
        }
    }

    private static class AnnotatedAbSynSyntaxVisitor extends SyntaxVisitor<AnnotatedAbSyn> {
        private final Env env;

        AnnotatedAbSynSyntaxVisitor(Env env) {
            this.env = env;
        }

        @Nullable
        @Override
        public AnnotatedAbSyn visitSyntax(Syntax s) {
            throw new SyntaxConversionException("No method to annotate " + s);
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
        public AnnotatedAbSyn visitComma(Comma comma) {
            throw new SyntaxConversionException("Can't annotate comma " + comma);
        }

        @Override
        public AnnotatedAbSyn visitDeclaration(DeclareNode<?> node) {
            return AnnotatedAbSyn.newDeclare(AnnotatedId.newAnnotatedId(env, Symbol._MINUS_(node.lhs().name())),
                    node.rhs().accept(this));
        }
    }
}
