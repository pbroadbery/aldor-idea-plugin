package aldor;

import aldor.psi.AldorId;
import aldor.psi.AldorJxleftAtom;
import aldor.psi.AldorLiteral;
import aldor.psi.AldorVisitor;
import aldor.psi.JxrightElement;
import aldor.psi.NegationElement;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class AldorPsiUtils {

    public static Syntax parse(PsiElement elt) {
        //Jxright_Molecule ::= Jxleft_Molecule Jxright_Atom? | KW_Not Jxright_Atom
        //Jxright_Atom ::= Jxleft_Atom Jxright_Atom? | KW_Not Jxright_Atom

        final Deque<List<Syntax>> visitStack = new ArrayDeque<>();
        visitStack.add(Lists.newArrayList());
        elt.accept(new AldorPsiSyntaxVisitor(visitStack));

        return visitStack.getFirst().get(0);
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
                throw new IllegalStateException("Expecting something");
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
        Apply(List<Syntax> arguments) {
            super(arguments);
        }

        @Override
        String name() {
            return "Apply";
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
        private final AldorId id;
        private final String text;

        Id(AldorId id, String text) {
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