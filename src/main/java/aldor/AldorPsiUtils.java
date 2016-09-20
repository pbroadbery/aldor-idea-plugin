package aldor;

import aldor.psi.AldorId;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorInfixedExpr;
import aldor.psi.AldorInfixedTok;
import aldor.psi.AldorJxleftAtom;
import aldor.psi.AldorLiteral;
import aldor.psi.AldorParened;
import aldor.psi.AldorVisitor;
import aldor.psi.JxrightElement;
import aldor.psi.NegationElement;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class AldorPsiUtils {
    private static final Logger LOG = Logger.getInstance(AldorPsiUtils.class);

    @Nullable
    public static Syntax parse(PsiElement elt) {
        try {
            final Deque<List<Syntax>> visitStack = new ArrayDeque<>();
            visitStack.add(Lists.newArrayList());
            elt.accept(new AldorPsiSyntaxVisitor(visitStack));
            return visitStack.getFirst().get(0);
        }
        catch (RuntimeException e) {
            LOG.error("Failed to parse " + elt.getText() + " " + elt, e);
            return null;
        }
    }

    public static boolean containsElement(PsiElement element, IElementType eltType) {
        if (element.getNode().getElementType().equals(eltType)) {
            return true;
        }
        for (PsiElement child: element.getChildren()) {
            if (containsElement(child, eltType)) {
                return true;
            }
        }
        return false;
    }

    private static final class AldorPsiSyntaxVisitor extends AldorVisitor {
        private final Deque<List<Syntax>> visitStack;

        private AldorPsiSyntaxVisitor(Deque<List<Syntax>> visitStack) {
            this.visitStack = visitStack;
        }

        @Override
        public void visitElement(PsiElement element) {
            element.acceptChildren(this);
        }

        @Override
        public void visitNegationElement(@NotNull NegationElement o) {
            visitStack.peek().add(new Other(o.getLastChild()));
        }

        @Override
        public void visitJxrightElement(@NotNull JxrightElement o) {
            List<Syntax> fnOrAtom = Lists.newArrayList();
            visitStack.push(fnOrAtom);
            o.acceptChildren(this);
            visitStack.pop();
            if (fnOrAtom.isEmpty()) {
                throw new IllegalStateException("Expecting something from " + o.getText());
            }

            if (fnOrAtom.size() == 1) {
                visitStack.peek().add(fnOrAtom.get(0));
            } else {
                Syntax syntax = new Apply(fnOrAtom);
                visitStack.peek().add(syntax);
            }
        }

        @Override
        public void visitJxleftAtom(@NotNull AldorJxleftAtom o) {
            List<Syntax> opsAndArgs = Lists.newArrayList();
            visitStack.push(opsAndArgs);
            o.acceptChildren(this);
            List<Syntax> last = visitStack.pop();
            //noinspection ObjectEquality
            assert last == opsAndArgs;

            if (opsAndArgs.isEmpty()) {
                throw new IllegalStateException("oops");
            }
            if (opsAndArgs.size() == 1) {
                visitStack.peek().add(opsAndArgs.get(0));
            } else {
                Syntax all = opsAndArgs.get(opsAndArgs.size() - 1);
                for (Syntax syntax : Lists.reverse(opsAndArgs).subList(1, opsAndArgs.size() - 2)) {
                    all = new Apply(Lists.newArrayList(syntax, all));
                }
                visitStack.peek().add(all);
            }
        }

        @Override
        public void visitId(@NotNull AldorId o) {
            visitStack.peek().add(new Id(o, o.getText()));
        }

        @Override
        public void visitLiteral(@NotNull AldorLiteral o) {
            visitStack.peek().add(new Literal(o.getText(), o));
        }

        @Override
        public void visitParened(@NotNull AldorParened parened) {
            List<Syntax> parenContent = Lists.newArrayList();
            visitStack.push(parenContent);
            parened.acceptChildren(this);
            List<Syntax>  last = visitStack.pop();
            //noinspection ObjectEquality
            assert last == parenContent;
            final Syntax next;
            if (parenContent.size() == 1) {
                next = parenContent.get(0);
            } else {
                next = new Comma(parenContent);
            }
            visitStack.peek().add(next);
        }

        @Override
        public void visitInfixedExpr(@NotNull AldorInfixedExpr expr) {
            List<Syntax> exprContent = Lists.newArrayList();
            visitStack.push(exprContent);
            expr.acceptChildren(this);
            List<Syntax>  last = visitStack.pop();
            //noinspection ObjectEquality
            assert last == exprContent;
            Syntax lhs = exprContent.get(0);
            for (int i=1; i<exprContent.size(); i+=2) {
                Syntax op = exprContent.get(i);
                lhs = new InfixApply(op, lhs, exprContent.get(i+1));
            }
            visitStack.peek().add(lhs);
        }

        @Override
        public void visitInfixedTok(@NotNull AldorInfixedTok tok) {
            visitStack.peek().add(new Id(tok, tok.getText()));
        }
    }


    @SuppressWarnings("AbstractClassNamingConvention")
    public abstract static class Syntax {
        abstract String name();
    }

    public abstract static class SyntaxNode extends Syntax {
        private final List<Syntax> arguments;

        protected SyntaxNode(List<Syntax> arguments) {
            this.arguments = arguments;
        }

        @Override
        public String toString() {
            return "(" + name() + " " + Joiner.on(" ").join(arguments) + ")";
        }
    }

    private static class Apply extends SyntaxNode {
        Apply(@NotNull List<Syntax> arguments) {
            super(arguments);
        }

        @Override
        String name() {
            return "Apply";
        }
    }


    private static class InfixApply extends SyntaxNode {
        InfixApply(@NotNull Syntax op, Syntax lhs, Syntax rhs) {
            super(Lists.newArrayList(op, lhs, rhs));
        }

        @Override
        String name() {
            return "InfixApply";
        }
    }


    private static class Comma extends SyntaxNode {
        Comma(@NotNull  List<Syntax> arguments) {
            super(arguments);
        }

        @Override
        String name() {
            return "Comma";
        }
    }

    private static class Other extends Syntax {
        private final PsiElement other;

        Other(PsiElement other) {
            this.other = other;
        }

        @Override
        String name() {
            return "Other";
        }
    }

    private static class Id extends Syntax {
        private final AldorIdentifier id;
        private final String text;

        Id(AldorIdentifier id, String text) {
            this.id = id;
            this.text = text;
        }

        @Override
        String name() {
            return "Id";
        }

        @Override
        public String toString() {
            return text;
        }

    }

    private static class Literal extends Syntax {
        private final String text;
        private final AldorLiteral literal;

        Literal(String text, AldorLiteral literal) {
            this.text = text;
            this.literal = literal;
        }

        @Override
        String name() {
            return "Literal";
        }

        @Override
        public String toString() {
            return name();
        }
    }
}