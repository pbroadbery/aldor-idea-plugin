package aldor.syntax;

import aldor.lexer.AldorTokenType;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Id;
import aldor.syntax.components.Other;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class SyntaxPrinter {
    private static final SyntaxPrinter instance = new SyntaxPrinter();

    private SyntaxPrinter() {

    }

    public static SyntaxPrinter instance() {
        return instance;
    }

    public void print(PrintWriter pw, Syntax syntax) {
        syntax.accept(new SyntaxPrintVisitor(pw));
    }

    public String toString(Syntax exporter) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        print(pw, exporter);
        return sw.toString();
    }

    private final class SyntaxPrintVisitor extends SyntaxVisitor<Void> {
        private final PrintWriter pw;

        private SyntaxPrintVisitor(PrintWriter pw) {
            this.pw = pw;
        }

        @Nullable
        @Override
        public Void visitSyntax(Syntax syntax) {
            return null;
        }

        @Override
        public Void visitId(Id id) {
            write(id.symbol());
            return null;
        }

        @Override
        public Void visitApply(Apply apply) {
            printApply(this, apply);
            return null;
        }

        @Override
        public Void visitComma(Comma comma) {
            printComma(this, comma);
            return null;
        }

        @Override
        public Void visitOther(Other other) {
            pw.write(other.toString());
            return null;
        }

        public void write(String s) {
            pw.write(s);
        }
    }

    private void printApply(SyntaxPrintVisitor visitor, Apply apply) {
        Syntax operator = apply.operator();
        AldorTokenType opToken = tokenForSyntax(operator);
        boolean isInfix = (opToken != null) && opToken.isInfix();
        boolean isBracket = isBracket(operator);

        if (isBracket) {
            visitor.write("[");
            printCommaSeq(visitor, apply, apply.arguments());
            visitor.write("]");
        }
        else if (isInfix && (apply.arguments().size() == 2)) {
            Syntax lhs = apply.arguments().get(0);
            Syntax rhs = apply.arguments().get(1);
            printWithParens(visitor, apply, lhs);
            visitor.write(" ");
            operator.accept(visitor);
            visitor.write(" ");
            printWithParens(visitor, apply, rhs);
        }
        else {
            Syntax argument0 = apply.arguments().get(0);
            printWithParens(visitor, apply, operator);
            if (apply.arguments().isEmpty()) {
                visitor.write("()");
            }
            else if (apply.arguments().size() == 1) {
                if (argument0.is(Comma.class)) {
                    visitor.write("(");
                    argument0.accept(visitor);
                    visitor.write(")");
                }
                else {
                    // Some operators might not want a space. tough.
                    visitor.write(" ");
                    printWithParens(visitor, apply, argument0);
                }
            }
        }
    }

    boolean isBracket(Syntax operator) {
        return operator.is(Id.class) && operator.as(Id.class).symbol().equals("bracket");
    }

    private void printWithParens(SyntaxPrintVisitor visitor, Syntax outer, Syntax argument) {
        if (!needsParens(outer, argument)) {
            argument.accept(visitor);
        } else {
            visitor.write("(");
            argument.accept(visitor);
            visitor.write(")");
        }
    }

    private AldorTokenType tokenForSyntax(Syntax syntax) {
        return syntax.tokenType();
    }

    private void printComma(SyntaxPrintVisitor visitor, Comma comma) {
        if (comma.children().isEmpty()) {
            visitor.write("()");
        }
        else if (comma.children().size() == 1) {
            comma.children().get(0).accept(visitor);
        }
        else {
            printCommaSeq(visitor, comma, comma.children());
        }
    }

    private void printCommaSeq(SyntaxPrintVisitor visitor, Syntax outer, List<Syntax> children) {
        String sep = "";
        for (Syntax syntax: children) {
            visitor.write(sep);
            sep = ", ";
            printWithParens(visitor, outer, syntax);
        }
    }

    private boolean needsParens(Syntax outer, Syntax inner) {
        return outer.accept(new NeedParenVisitor(inner));
    }

    private class NeedParenVisitor extends SyntaxVisitor<Boolean> {
        private final Syntax inner;

        private NeedParenVisitor(Syntax inner) {
            this.inner = inner;
        }

        @NotNull
        @Override
        public Boolean visitSyntax(Syntax syntax) {
            if (inner instanceof Comma) {
                return true;
            }
            return false;
        }

        @Override
        public Boolean visitApply(Apply apply) {
            if (inner.is(Id.class)) {
                return false;
            }
            return true;
        }
    }
}

