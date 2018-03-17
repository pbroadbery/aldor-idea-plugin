package aldor.spad;

import aldor.syntax.Syntax;
import aldor.syntax.components.Id;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpadLibrary {

    List<Syntax> parentCategories(Syntax syntax);
    List<Operation> operations(Syntax syntax);

    @NotNull
    Syntax normalise(@NotNull Syntax syntax);

    List<Syntax> allTypes();

    String definingFile(Id id);

    class Operation {
        private final String name;
        private final Syntax type;
        private final Syntax condition;
        private final Syntax exporter;
        @Nullable
        private final PsiElement implementation;
        @Nullable
        private final PsiElement declaration;

        public Operation(String name, Syntax type, Syntax condition, Syntax exporter, @Nullable PsiElement declaration) {
            this.name = name;
            this.type = type;
            this.condition = condition;
            this.exporter = exporter;
            this.declaration = declaration;
            this.implementation = null;
        }

        public Operation(String name, Syntax type, Syntax condition, Syntax exporter) {
            this(name, type, condition, exporter, null);
        }

        @Override
        public String toString() {
            return "{Op: " + name + " " + type + "}";
        }

        public String name() {
            return name;
        }

        public Syntax type() {
            return type;
        }

        @Nullable
        public PsiElement declaration() {
            return declaration;
        }

        @Nullable
        public PsiElement implementation() {
            return implementation;
        }
    }


}
