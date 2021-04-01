package aldor.syntax;

import aldor.psi.AldorAddPart;
import aldor.psi.AldorAddPrecedenceExpr;
import aldor.psi.AldorBinaryWithExpr;
import aldor.psi.AldorBracketed;
import aldor.psi.AldorColonExpr;
import aldor.psi.AldorDeclPart;
import aldor.psi.AldorDefine;
import aldor.psi.AldorE14;
import aldor.psi.AldorExpPrecedenceExpr;
import aldor.psi.AldorExpr;
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
import aldor.psi.AldorRightArrow1002Expr;
import aldor.psi.AldorTimesPrecedenceExpr;
import aldor.psi.AldorUnaryWithExpr;
import aldor.psi.AldorWith;
import aldor.psi.JxrightElement;
import aldor.psi.NegationElement;
import aldor.psi.ReturningAldorVisitor;
import aldor.psi.SpadBinaryOp;
import aldor.syntax.components.Add;
import aldor.syntax.components.AldorDeclare;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Define;
import aldor.syntax.components.EnumList;
import aldor.syntax.components.Id;
import aldor.syntax.components.InfixedId;
import aldor.syntax.components.Literal;
import aldor.syntax.components.Other;
import aldor.syntax.components.OtherSx;
import aldor.syntax.components.QuotedSymbol;
import aldor.syntax.components.SpadDeclare;
import aldor.syntax.components.With;
import aldor.util.sexpr.SExpression;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
            return visitStack.stream().findFirst().flatMap(x -> x.stream().findFirst()).orElse(null);
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (RuntimeException e) {
            LOG.error("Failed to parse " + elt.getText() + " " + elt, e);
            logPsi(elt);
            return null;
        }
    }

    public enum SurroundType { Any, Leading }

    @Nullable
    public static Syntax surroundingApplication(PsiElement element, SurroundType type) {
        SurroundingApplicationVisitor visitor = new SurroundingApplicationVisitor(type);
        return visitor.apply(element);
    }

    @SuppressWarnings("CyclicClassDependency")
    private static final class SurroundingApplicationVisitor extends ReturningAldorVisitor<Syntax> {
        private final SurroundType type;

        private SurroundingApplicationVisitor(SurroundType leadingArgument) {
            this.type = leadingArgument;
        }

        @Override
        public void visitElement(PsiElement element) {
            super.visitElement(element);
            if (element.getParent() == null) {
                returnValue(null);
            }
            else if ((this.type == SurroundType.Leading) && (element.getStartOffsetInParent() != 0)) {
                returnValue(null);
            }
            else {
                element.getParent().accept(this);
            }
        }

        @Override
        public void visitFile(PsiFile file) {
            returnValue(null);
        }

        @Override
        public void visitJxrightElement(@NotNull JxrightElement o) {
            visitApplyBuilder(o);
        }

        @Override
        public void visitJxleftAtom(@NotNull AldorJxleftAtom o) {
            visitApplyBuilder(o);
        }

        @Override
        public void visitInfixedExpression(@NotNull AldorInfixedExpression expr) {
            visitApplyBuilder(expr);
        }

        @Override
        public void visitBracketed(@NotNull AldorBracketed brackets) {
            visitApplyBuilder(brackets);
        }

        @Override
        public void visitRightArrow1002Expr(@NotNull AldorRightArrow1002Expr rightArrow1002Expr) {
            visitApplyBuilder(rightArrow1002Expr);
        }

        @Override
        public void visitRelExpr(@NotNull AldorRelExpr addPrecedenceExpr) {
            visitBinaryOp(addPrecedenceExpr);
        }

        public void visitBinaryOp(PsiElement expr) {
            visitApplyBuilder(expr);
        }

        private void visitApplyBuilder(@NotNull PsiElement o) {
            Syntax syntax = parse(o);
            if (syntax == null) {
                returnValue(null);
            } else if (syntax.is(Apply.class)) {
                returnValue(syntax);
            } else {
                visitElement(o);
            }
        }

        @Override
        public void visitId(@NotNull AldorId id) {
            visitElement(id);
            if (returnValue() == null) {
                returnValue(parse(id));
            }
        }
    }

    @SuppressWarnings("OverlyCoupledClass")
    private static final class AldorPsiSyntaxVisitor extends AldorRecursiveVisitor {
        private final Deque<List<Syntax>> visitStack;
        private boolean leftOfDeclare = false; // really should be "context"

        private AldorPsiSyntaxVisitor(Deque<List<Syntax>> visitStack) {
            this.visitStack = visitStack;
        }

        @Override
        public void visitNegationElement(@NotNull NegationElement o) {
            parentParts().add(new Other(o.getLastChild()));
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
                parentParts().add(fnOrAtom.get(0));
            }
            else {
                // FIXME: Obviously wrong(!)
                parentParts().add(new Other(o));
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
                parentParts().add(fnOrAtom.get(0));
            } else {
                if (fnOrAtom.get(1).is(Comma.class)) {
                    List<Syntax> args = fnOrAtom.get(1).as(Comma.class).children();
                    Syntax op = fnOrAtom.get(0);
                    fnOrAtom = new ArrayList<>(1+args.size());
                    fnOrAtom.add(op);
                    fnOrAtom.addAll(args);
                }
                Syntax syntax = new Apply(o, fnOrAtom);
                parentParts().add(syntax);
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
                parentParts().add(new Other(o));
            }
            else if (opsAndArgs.size() == 1) {
                parentParts().add(opsAndArgs.get(0));
            } else if (opsAndArgs.size() == 2) {
                parentParts().add(new Apply(o, opsAndArgs));
            }
            else {
                Syntax all = opsAndArgs.get(opsAndArgs.size() - 1);
                for (Syntax syntax : Lists.reverse(opsAndArgs).subList(1, opsAndArgs.size() - 2)) {
                    all = new Apply((PsiElement) null, Lists.newArrayList(syntax, all));
                }
                parentParts().add(all);
            }
        }

        @NotNull
        private List<Syntax> parentParts() {
            assert visitStack.peek() != null;
            return visitStack.peek();
        }

        @Override
        public void visitQuotedIds(@NotNull AldorQuotedIds ids) {
            List<Syntax> last = buildChildren(ids);
            parentParts().add(new EnumList(ids, last));
        }

        @Override
        public void visitId(@NotNull AldorId o) {
            parentParts().add(new Id(o));
        }

        @Override
        public void visitLiteral(@NotNull AldorLiteral o) {
            String text = o.getText();
            Syntax elt = leftOfDeclare ? new InfixedId(o) : new Literal(text, o);
            parentParts().add(elt);
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
            parentParts().add(next);
        }


        @Override
        public void visitColonExpr(@NotNull AldorColonExpr colonExpr) {
            List<Syntax> content = Lists.newArrayList();
            visitStack.push(content);

            List<AldorExpr> ll = colonExpr.getExprList();
            if (ll.size() >= 1) {
                this.leftOfDeclare = true;
                ll.get(0).accept(this);
                this.leftOfDeclare = false;
            }
            for (AldorExpr e: ll.subList(1, ll.size())) {
                e.accept(this);
            }
            List<Syntax> last = visitStack.pop();
            Syntax result;
            if (last.size() != 2) {
                result = new OtherSx(SExpression.string("Odd declaration. Giving up " + last));
            }
            else {
                result = new SpadDeclare(colonExpr, last);
            }
            parentParts().add(result);
        }

        @Override
        public void visitDeclPart(@NotNull AldorDeclPart decl) {
            List<Syntax> content = Lists.newArrayList();
            visitStack.push(content);

            this.leftOfDeclare = true;
            if (decl.getInfixedExpression() != null) {
                decl.getInfixedExpression().accept(this);
            }
            if (decl.getInfixedExprs() != null) {
                decl.getInfixedExprs().accept(this);
            }
            this.leftOfDeclare = false;

            decl.getType().accept(this);

            List<Syntax> last = visitStack.pop();

            Syntax result = new AldorDeclare(decl, last);
            parentParts().add(result);
        }

        @Override
        public void visitBinaryWithExpr(@NotNull AldorBinaryWithExpr o) {
            parentParts().add(new With(o));
        }

        @Override
        public void visitUnaryWithExpr(@NotNull AldorUnaryWithExpr o) {
            parentParts().add(new With(o));
        }

        @Override
        public void visitAddPart(@NotNull AldorAddPart o) {
            parentParts().add(new Add(o));
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
            parentParts().add(lhs);
        }

        @Override
        public void visitInfixedTok(@NotNull AldorInfixedTok tok) {
            parentParts().add(new Id(tok));
        }

        @Override
        public void visitDefine(@NotNull AldorDefine define) {
            List<Syntax> last = buildChildren(define);
            parentParts().add(new Define(define, last));
        }

        @Override
        public void visitBracketed(@NotNull AldorBracketed brackets) {
            List<Syntax> last = buildChildren(brackets);
            Syntax implicitOp = createImplicitId("bracket");
            last.add(0, implicitOp);
            parentParts().add(new Apply(brackets, last));
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
        public void visitRightArrow1002Expr(@NotNull AldorRightArrow1002Expr rightArrow1002Expr) {
            List<Syntax> last = new ArrayList<>(buildChildren(rightArrow1002Expr));
            last.add(0, createImplicitId(rightArrow1002Expr.getKWRArrow(), "->"));
            parentParts().add(new Apply(rightArrow1002Expr, last));
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
            parentParts().add(new Apply(expr, args));
        }

        private List<Syntax> buildChildren(PsiElement psi) {
            List<Syntax> content = Lists.newArrayList();
            visitStack.push(content);
            psi.acceptChildren(this);
            return visitStack.pop();
        }

        @Override
        public void visitQuoteExpr(@NotNull AldorQuoteExpr o) {
            parentParts().add(new QuotedSymbol(o));
        }
    }

    public static String prettyPrint(Syntax syntax) {
        // FIXME: Temporary
        return "{pretty: " + syntax + "}";
    }

}
