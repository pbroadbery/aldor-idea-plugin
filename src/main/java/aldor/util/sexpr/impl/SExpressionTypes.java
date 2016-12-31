package aldor.util.sexpr.impl;

import aldor.util.AssociationList;
import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SxType;
import aldor.util.sexpr.SymbolPolicy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SExpressionTypes {
    @SuppressWarnings("FieldHasSetterButNoGetter")
    public static class Cons extends SExpression {
        @NotNull
        private SExpression car;
        @NotNull
        private SExpression cdr;

        public Cons(@NotNull SExpression car, @NotNull SExpression cdr) {
            super(SxType.Cons);
            this.car = car;
            this.cdr = cdr;
        }

        @Override
        public int hashCode(int level) {
            if (level > 10) {
                return -1;
            }
            return car.hashCode(level + 1) + cdr.hashCode(level + 1);
        }

        @Override
        protected boolean innerEqual(SExpression other) {
            return car.equals(other.car()) && cdr.equals(other.cdr());
        }

        @Override
        public final SExpression car() {
            return car;
        }

        @Override
        public final SExpression cdr() {
            return cdr;
        }

        @Override
        public void setCar(@NotNull SExpression car) {
            this.car = car;
        }

        @Override
        public void setCdr(@NotNull SExpression cdr) {
            this.cdr = cdr;
        }

        @Override
        public List<SExpression> asList() {
            return new SExpressionList(this);
        }

        @Override
        public Map<SExpression, SExpression> asAssociationList() {
            return new AssociationList(this.asList());
        }

        @Override
        public void write(Writer w, SymbolPolicy mode) throws IOException {
            w.write("(");
            SExpression current = this;
            boolean done = false;
            while (!done) {
                current.car().write(w, mode);

                if (current.cdr().isOfType(SxType.Cons)) {
                    w.write(" ");
                    current = current.cdr();
                } else if (current.cdr().isNull()) {
                    done = true;
                } else {
                    w.write(" . ");
                    current.cdr().write(w, mode);
                    done = true;
                }
            }
            w.write(")");
        }

    }

    public abstract static class AbstractAtom<T> extends SExpression {
        private final T value;

        AbstractAtom(SxType<?> type, T value) {
            super(type);
            this.value = value;
        }

        protected T value() {
            return value;
        }

        @Override
        public int hashCode(int level) {
            return value.hashCode();
        }

        @Override
        protected boolean innerEqual(SExpression other) {
            @SuppressWarnings("unchecked")
            AbstractAtom<T> otherAtom = (AbstractAtom<T>) other;
            return this.value().equals(otherAtom.value());
        }

    }

    public static class IntegerAtom extends AbstractAtom<Integer> {
        public IntegerAtom(int n) {
            super(SxType.Integer, n);
        }

        @Override
        public int integer() {
            return value();
        }

        @Override
        public void write(Writer w, SymbolPolicy mode) throws IOException {
            w.append(String.valueOf(value()));
        }

    }

    public static class StringAtom extends AbstractAtom<String> {
        public StringAtom(String text) {
            super(SxType.String, text);
        }

        @SuppressWarnings("QuestionableName")
        @Override
        public String string() {
            return value();
        }

        @Override
        public void write(Writer w, SymbolPolicy mode) throws IOException {
            w.append("\"");
            w.append(string());
            w.append("\"");
        }
    }

    public static class SymbolAtom extends AbstractAtom<String> {
        public SymbolAtom(String text) {
            super(SxType.Symbol, text);
        }

        @Override
        public String symbol() {
            return value();
        }

        @Override
        public void write(Writer w, SymbolPolicy mode) throws IOException {
            if (needsEscape(mode)) {
                w.append("|");
                w.append(value());
                w.append("|");
            }
            else {
                w.append(this.value());
            }
        }

        private boolean needsEscape(SymbolPolicy mode) {
            if (mode == SymbolPolicy.NORMAL) {
                return false;
            }
            else {
                //noinspection StringToUpperCaseOrToLowerCaseWithoutLocale
                return !value().equals(value().toUpperCase());
            }
        }
    }

    public static class Nil extends SExpression {
        public Nil() {
            super(SxType.Nil);
        }

        @Override
        public void write(Writer w, SymbolPolicy mode) throws IOException {
            w.append("()");
        }

        @Override
        public int hashCode(int level) {
            return "Nil".hashCode();
        }

        @Override
        protected boolean innerEqual(SExpression other) {
            return true;
        }

        @Override
        public List<SExpression> asList() {
            return Collections.emptyList();
        }

        @Override
        public Map<SExpression, SExpression> asAssociationList() {
            return Collections.emptyMap();
        }

    }

    static final SExpression nil = new Nil();

    public static SExpression nil() {
        return nil;
    }

}
