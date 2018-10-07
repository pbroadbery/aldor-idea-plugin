package aldor.spad;

import aldor.syntax.Syntax;
import aldor.syntax.components.Id;
import aldor.typelib.Env;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpadLibrary {

    List<Syntax> parentCategories(Syntax syntax);
    List<Operation> operations(Syntax syntax);

    @NotNull
    Syntax normalise(@NotNull Syntax syntax);

    @NotNull
    List<Syntax> allTypes();

    String definingFile(Id id);

    @NotNull
    Env environment();

    GlobalSearchScope scope(Project project);

    void addDependant(SpadLibrary lib);

    void needsReload();

    class Operation {
        @NotNull
        private final String name;
        @NotNull
        private final Syntax type;
        private final Syntax condition;
        private final Syntax exporter;
        @Nullable
        private final PsiElement implementation;
        @Nullable
        private final PsiElement declaration; // Note that if the operation is inherited, then this is the defining form of the exporter.
        @Nullable
        private final PsiElement containingForm;

        public Operation(@NotNull String name, @NotNull Syntax type, Syntax condition, Syntax exporter, @Nullable PsiElement declaration, @Nullable PsiElement containingForm) {
            this.name = name;
            this.type = type;
            this.condition = condition;
            this.exporter = exporter;
            this.declaration = declaration;
            this.implementation = null;
            this.containingForm = containingForm;
        }

        public Operation(String name, Syntax type, Syntax condition, Syntax exporter) {
            this(name, type, condition, exporter, null, null);
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

        public PsiElement containingForm() {
            return containingForm;
        }
    }
}
