package aldor.util;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SxFormUtils {

    public static SxTagForm tagged() {
        return new SxTagForm();
    }

    public static SxForm name(@Nonnull String id) {
        return new SxLiteral(id);
    }

    public static SxForm number(Integer n) {
        return new SxLiteral(Integer.toString(n));
    }

    public static SxForm file(File fname) {
        return new SxLiteral("file:" + fname);
    }

    public static <T> SxForm asForm(T obj) {
        return (obj instanceof HasSxForm) ? ((HasSxForm) obj).sxForm() : stringified(obj.toString());
    }

    public static <T> Optional<SxForm> asFormMaybe(T obj) {
        return (obj instanceof HasSxForm) ? Optional.of(((HasSxForm) obj).sxForm()) : Optional.empty();
    }

    public static SxListForm list() {
        return new SxListForm(new ArrayList<>());
    }

    public static SxForm stringified(String toString) {
        return new SxLiteral("<<" + toString + ">>");
    }

    public static Collector<SxForm, ?, SxForm> collectList() {
        return Collectors.collectingAndThen(Collectors.toList(), l -> listFrCollection(l));
    }

    private static SxForm listFrCollection(List<SxForm> l) {
        return new SxListForm(l);
    }

    public static SxForm bool(boolean flg) {
        return new SxLiteral(Boolean.toString(flg));
    }

    public static class SxLiteral implements SxForm {
        private final String literal;

        public SxLiteral(String literal) {
            this.literal = literal;
        }

        @Override
        public String asSExpression() {
            return literal;
        }


        @Override
        public String toString() {
            return asSExpression();
        }
    }

    public static class SxListForm implements SxForm {
        private final List<SxForm> children;

        SxListForm(List<SxForm> children) {
            this.children = children;
        }

        @Contract(pure = true)
        public SxListForm add(SxForm form) {
            return new SxListForm(Stream.concat(children.stream(), Stream.of(form)).collect(Collectors.toList()));
        }

        @Override
        public String asSExpression() {
            String text = children.stream().map(x -> x.asSExpression()).collect(Collectors.joining(" "));
            return "(" + text + ")";
        }

        @Override
        public String toString() {
            return asSExpression();
        }
    }

    public static class SxTagForm implements SxForm {
        private final List<Pair<String, SxForm>> formForName;

        public SxTagForm() {
            this.formForName = Collections.emptyList();
        }


        public SxTagForm(List<Pair<String, SxForm>> formForName) {
            this.formForName = formForName;
        }

        public SxTagForm with(String name, SxForm form) {
            return new SxTagForm(Stream.concat(formForName.stream(), Stream.of(new Pair<>(name, form))).collect(Collectors.toList()));
        }

        @Override
        public String toString() {
            return asSExpression();
        }

        @Override
        public String asSExpression() {
            String text = formForName.stream().map(p -> String.format("(%s --> %s)", p.getFirst(), p.getSecond().asSExpression())).collect(Collectors.joining(" "));
            return "(" + text +")";
        }
    }
}
