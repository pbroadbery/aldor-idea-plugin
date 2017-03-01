package aldor.syntax;

import aldor.psi.AldorAddPart;
import aldor.psi.AldorAddPrecedenceExpr;
import aldor.psi.AldorBracketed;
import aldor.psi.AldorColonExpr;
import aldor.psi.AldorDeclPart;
import aldor.psi.AldorDefine;
import aldor.psi.AldorE14;
import aldor.psi.AldorExpPrecedenceExpr;
import aldor.psi.AldorId;
import aldor.psi.AldorInfixedExpression;
import aldor.psi.AldorInfixedTok;
import aldor.psi.AldorJxleftAtom;
import aldor.psi.AldorLiteral;
import aldor.psi.AldorParened;
import aldor.psi.AldorQuoteExpr;
import aldor.psi.AldorQuotedIds;
import aldor.psi.AldorRecursiveVisitor;
import aldor.psi.AldorRelExpr;
import aldor.psi.AldorTimesPrecedenceExpr;
import aldor.psi.AldorWithPart;
import aldor.psi.JxrightElement;
import aldor.psi.NegationElement;
import aldor.psi.SpadBinaryOp;
import aldor.syntax.components.Add;
import aldor.syntax.components.AldorDeclare;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Define;
import aldor.syntax.components.EnumList;
import aldor.syntax.components.Id;
import aldor.syntax.components.Literal;
import aldor.syntax.components.Other;
import aldor.syntax.components.QuotedSymbol;
import aldor.syntax.components.SpadDeclare;
import aldor.syntax.components.With;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static aldor.psi.AldorPsiUtils.logPsi;
import static aldor.syntax.components.Id.createImplicitId;

/**
 * Turns Psi into lhsSyntax
 */
public final class SyntaxPsiParser {
    private static final Logger LOG = Logger.getInstance(SyntaxPsiParser.class);

    @Nullable
    public static Syntax parse(PsiElement elt) {
        try {
            final Deque<List<Syntax>> visitStack = new ArrayDeque<>();
            visitStack.add(Lists.newArrayList());
            elt.accept(new AldorPsiSyntaxVisitor(visitStack));
            return visitStack.getFirst().get(0);
        }
        catch (RuntimeException e) {
            logPsi(elt);
            LOG.error("Failed to parse " + elt.getText() + " " + elt, e);
            return null;
        }
    }

    @SuppressWarnings("OverlyCoupledClass")
    private static final class AldorPsiSyntaxVisitor extends AldorRecursiveVisitor {
        private final Deque<List<Syntax>> visitStack;

        private AldorPsiSyntaxVisitor(Deque<List<Syntax>> visitStack) {
            this.visitStack = visitStack;
        }

        @Override
        public void visitNegationElement(@NotNull NegationElement o) {
            visitStack.peek().add(new Other(o.getLastChild()));
        }

        /**
         * Scan the following:
         * E14 ::= ((E15? (WithPart | AddPart)) | (E15 (KW_Except E15 | KW_Throw E15| Nothing))) (WithPart | AddPart)*
         * Nothing to do with the canary wharf tourist board.
         */
        @Override
        public void visitE14(@NotNull AldorE14 o) {
            List<Syntax> fnOrAtom = Lists.newArrayList();
            visitStack.push(fnOrAtom);
            o.acceptChildren(this);
            visitStack.pop();
            if (fnOrAtom.size() == 1) {
                visitStack.peek().add(fnOrAtom.get(0));
            }
            else {
                // FIXME: Obviously wrong(!)
                visitStack.peek().add(new Other(o));
            }
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
                // We're almost surely throwing something away here...
                visitStack.peek().add(new Other(o));
            }
            else if (opsAndArgs.size() == 1) {
                visitStack.peek().add(opsAndArgs.get(0));
            } else if (opsAndArgs.size() == 2) {
                visitStack.peek().add(new Apply(o, opsAndArgs));
            }
            else {
                Syntax all = opsAndArgs.get(opsAndArgs.size() - 1);
                for (Syntax syntax : Lists.reverse(opsAndArgs).subList(1, opsAndArgs.size() - 2)) {
                    all = new Apply(null, Lists.newArrayList(syntax, all));
                }
                visitStack.peek().add(all);
            }
        }

        @Override
        public void visitQuotedIds(@NotNull AldorQuotedIds ids) {
            List<Syntax> last = buildChildren(ids);
            visitStack.peek().add(new EnumList(ids, last));
        }

        @Override
        public void visitId(@NotNull AldorId o) {
            visitStack.peek().add(new Id(o));
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
            List<Syntax> last = buildChildren(decl);
            Syntax result = new AldorDeclare(decl, last);
            visitStack.peek().add(result);
        }


        @Override
        public void visitColonExpr(@NotNull AldorColonExpr colonExpr) {
            List<Syntax> last = buildChildren(colonExpr);
            Syntax result = new SpadDeclare(colonExpr, last);
            visitStack.peek().add(result);
        }


        @Override
        public void visitWithPart(@NotNull AldorWithPart o) {
            // This isn't ideal, and we can parse the underlying stuff, but
            // leave for the moment
            visitStack.peek().add(new With(o));
        }

        @Override
        public void visitAddPart(@NotNull AldorAddPart o) {
            visitStack.peek().add(new Add(o));
        }


        @Override
        public void visitInfixedExpression(@NotNull AldorInfixedExpression expr) {
            List<Syntax> exprContent = buildChildren(expr);
            Syntax lhs;
            int i=1;
            if ((exprContent.size() % 2) == 0) {
                lhs = new Apply(expr, exprContent.subList(0, 2));
                i++;
            }
            else {
                lhs = exprContent.get(0);
            }

            //noinspection ForLoopWithMissingComponent
            for (; i<exprContent.size(); i+=2) {
                Syntax op = exprContent.get(i);
                lhs = new Apply(expr, Lists.newArrayList(op, lhs, exprContent.get(i+1)));
            }
            visitStack.peek().add(lhs);
        }

        @Override
        public void visitInfixedTok(@NotNull AldorInfixedTok tok) {
            visitStack.peek().add(new Id(tok));
        }

        @Override
        public void visitDefine(@NotNull AldorDefine define) {
            List<Syntax> last = buildChildren(define);
            visitStack.peek().add(new Define(define, last));
        }

        @Override
        public void visitBracketed(@NotNull AldorBracketed brackets) {
            List<Syntax> last = buildChildren(brackets);
            Syntax implicitOp = createImplicitId("bracket");
            last.add(0, implicitOp);
            visitStack.peek().add(new Apply(brackets, last));
        }


        @Override
        public void visitExpPrecedenceExpr(@NotNull AldorExpPrecedenceExpr expPrecedenceExpr) {
            visitBinaryOp(expPrecedenceExpr);
        }

        @Override
        public void visitTimesPrecedenceExpr(@NotNull AldorTimesPrecedenceExpr addPrecedenceExpr) {
            visitBinaryOp(addPrecedenceExpr);
        }

        @Override
        public void visitAddPrecedenceExpr(@NotNull AldorAddPrecedenceExpr addPrecedenceExpr) {
            visitBinaryOp(addPrecedenceExpr);
        }

        @Override
        public void visitRelExpr(@NotNull AldorRelExpr addPrecedenceExpr) {
            visitBinaryOp(addPrecedenceExpr);
        }

        public void visitBinaryOp(SpadBinaryOp expr) {
            List<Syntax> args = Lists.newArrayList();
            visitStack.push(args);
            expr.getOp().accept(this);
            for (PsiElement elt: expr.getExprList()) {
                elt.accept(this);
            }
            visitStack.pop();
            visitStack.peek().add(new Apply(expr, args));
        }

        private List<Syntax> buildChildren(PsiElement psi) {
            List<Syntax> parenContent = Lists.newArrayList();
            visitStack.push(parenContent);
            psi.acceptChildren(this);
            return visitStack.pop();
        }

        @Override
        public void visitQuoteExpr(@NotNull AldorQuoteExpr o) {
            visitStack.peek().add(new QuotedSymbol(o));
        }
    }

    public static String prettyPrint(Syntax syntax) {
        // FIXME: Temporary
        return "{pretty: " + syntax + "}";
    }

}
