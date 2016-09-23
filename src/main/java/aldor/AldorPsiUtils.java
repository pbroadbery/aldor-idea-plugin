package aldor;

import aldor.psi.AldorDeclPart;
import aldor.psi.AldorId;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorInfixedExpr;
import aldor.psi.AldorInfixedTok;
import aldor.psi.AldorJxleftAtom;
import aldor.psi.AldorLiteral;
import aldor.psi.AldorParened;
import aldor.psi.AldorRecursiveVisitor;
import aldor.psi.JxrightElement;
import aldor.psi.NegationElement;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class AldorPsiUtils {
    private static final Logger LOG = Logger.getInstance(AldorPsiUtils.class);



    public static final int MAX_INDENT_DEPTH = 20;

    public static void logPsi(PsiElement psi) {
        logPsi(psi, 0);
    }

    // TODO: Remove most uses of this method
    @SuppressWarnings("SameParameterValue")
    static void logPsi(PsiElement psi, int i) {
        logPsi(psi, i, "");
    }
    static void logPsi(PsiElement psi, int depth, String lastStuff) {
        PsiElement[] children = psi.getChildren();
        int childCount = children.length;
        String text = (childCount == 0) ? psi.getText(): "";
        String spaces = Strings.repeat(" ", Math.min(depth, MAX_INDENT_DEPTH));
        if (childCount == 0) {
            System.out.println(spaces + "(psi: " + psi + " " + text + ")" + lastStuff);
            return;
        }
        System.out.println(spaces + "(psi: " + psi + " " + text);
        for (int i = 0; i < (childCount - 1); i++) {
            logPsi(children[i], depth+1, "");
        }
        logPsi(children[childCount -1], depth+1, ")" + lastStuff);
    }

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

    private static final class AldorPsiSyntaxVisitor extends AldorRecursiveVisitor {
        private final Deque<List<Syntax>> visitStack;

        private AldorPsiSyntaxVisitor(Deque<List<Syntax>> visitStack) {
            this.visitStack = visitStack;
        }

        @Override
        public void visitPsiElement(@NotNull PsiElement o) {
            System.out.println("Visit: " + o);
            super.visitPsiElement(o);
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
                Syntax syntax = new Apply(o, fnOrAtom);
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
                    all = new Apply(null, Lists.newArrayList(syntax, all));
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
                next = new Comma(parened, parenContent);
            }
            visitStack.peek().add(next);
        }

        @Override
        public void visitDeclPart(@NotNull AldorDeclPart decl) {
            List<Syntax> parenContent = Lists.newArrayList();
            visitStack.push(parenContent);
            decl.acceptChildren(this);
            List<Syntax> last = visitStack.pop();
            Syntax result = new Declaration(decl, last);
            visitStack.peek().add(result);
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
                lhs = new InfixApply(expr, op, lhs, exprContent.get(i+1));
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

        public abstract PsiElement psiElement();

        public abstract Iterable<Syntax> children();

        @Nullable
        public <T extends Syntax> T as(@NotNull  Class<T> clzz) {
            if (clzz.isAssignableFrom(this.getClass())) {
                return clzz.cast(this);
            }
            return null;
        }

        public <T extends Syntax> boolean is(@NotNull  Class<T> clzz) {
            return clzz.isAssignableFrom(this.getClass());
        }
    }

    public abstract static class SyntaxNode<SyntaxPsiElement extends PsiElement> extends Syntax {
        protected final List<Syntax> arguments;
        private final SyntaxPsiElement element;

        protected SyntaxNode(SyntaxPsiElement element, List<Syntax> arguments) {
            this.element = element;
            this.arguments = new ArrayList<>(arguments);
        }

        @Override
        public String toString() {
            return "(" + name() + " " + Joiner.on(" ").join(arguments) + ")";
        }

        @Override
        public SyntaxPsiElement psiElement() {
            return element;
        }

        @Override
        public Iterable<Syntax> children() {
            return arguments;
        }

        public Syntax child(int n) {
            return arguments.get(n);
        }
    }

    public abstract static class AnyApply<T extends PsiElement> extends SyntaxNode<T> {

        protected AnyApply(T element, List<Syntax> arguments) {
            super(element, arguments);
        }

        public abstract Syntax operator();
        public abstract List<Syntax> arguments();
    }

    public static class Apply extends AnyApply<JxrightElement> {
        Apply(JxrightElement element, @NotNull List<Syntax> arguments) {
            super(element, arguments);
        }

        @Override
        public Syntax operator() {
            return arguments.get(0);
        }

        @Override
        String name() {
            return "Apply";
        }

        @Override
        public List<Syntax> arguments() {
            return arguments.subList(1, arguments.size());
        }
    }


    private static class InfixApply extends AnyApply<PsiElement> {
        InfixApply(PsiElement element, @NotNull Syntax op, Syntax lhs, Syntax rhs) {
            super(element, Lists.newArrayList(op, lhs, rhs));
        }

        @Override
        String name() {
            return "InfixApply";
        }

        @Override
        public Syntax operator() {
            return arguments.get(0);
        }

        @Override
        public List<Syntax> arguments() {
            return arguments.subList(1, arguments.size());
        }
    }


    public static class Comma extends SyntaxNode<AldorParened> {
        Comma(AldorParened element, @NotNull List<Syntax> arguments) {
            super(element, arguments);
        }

        @Override
        String name() {
            return "Comma";
        }
    }

    public static class Declaration extends SyntaxNode<AldorDeclPart> {
        Declaration(AldorDeclPart element, @NotNull List<Syntax> arguments) {
            super(element, arguments);
        }

        public Syntax lhs() {
            return child(0);
        }

        public Syntax rhs() {
            return child(1);
        }

        @Override
        String name() {
            return "Decl";
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

        @Override
        public PsiElement psiElement() {
            return other;
        }

        @Override
        public Iterable<Syntax> children() {
            return Collections.emptyList();
        }
    }

    public static class Id extends Syntax {
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
        public PsiElement psiElement() {
            return id;
        }

        @Override
        public Iterable<Syntax> children() {
            return Collections.emptyList();
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
        public PsiElement psiElement() {
            return literal;
        }

        @Override
        public Iterable<Syntax> children() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return name();
        }
    }
}