package aldor.editor;

import aldor.language.AldorLanguage;
import aldor.language.SpadLanguage;
import aldor.psi.AldorAddPart;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorUnaryAdd;
import aldor.psi.AldorWith;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AldorBreadcrumbsProvider implements BreadcrumbsProvider {
    static final Language[] languages = {AldorLanguage.INSTANCE, SpadLanguage.INSTANCE};
    static final Map<Class<?>, VisitInfo<?>> visitInfoForClass = new HashMap<>();
    static {
        //visitInfoForClass.put(PsiElement.class, new NoBreadcrumbs());
        visitInfoForClass.put(AldorWith.class, new TokenBreadcrumb("with"));
        visitInfoForClass.put(AldorAddPart.class, new TokenBreadcrumb("add"));
        visitInfoForClass.put(AldorUnaryAdd.class, new TokenBreadcrumb("add"));
        //visitInfoForClass.put(aldorDefault, new TokenBreadcrumb("Default"));
        visitInfoForClass.put(AldorDefine.class, new DefineBreadcrumb());
    }

    private <T extends PsiElement> VisitInfo<T> visitInfoForClass0(Class<?> clzz) {
        while (true) {
            if (visitInfoForClass.containsKey(clzz)) {
                return (VisitInfo<T>) visitInfoForClass.get(clzz);
            }
            for (Class<?> parentInterface : clzz.getInterfaces()) {
                VisitInfo<?> info = visitInfoForClass(parentInterface);
                if (info != null) {
                    return (VisitInfo<T>) info;
                }
            }
            clzz = clzz.getSuperclass();
            if (clzz == null) {
                return null;
            }

        }
    }

    private <T extends PsiElement> VisitInfo<? super T> visitInfoForClass(Class<?> clzz) {
        VisitInfo<PsiElement> info = visitInfoForClass0(clzz);
        if (info == null) {
            info = new NoBreadcrumbs<>();
        }
        visitInfoForClass.put(clzz, info);
        return info;
    }

    @Override
    public Language[] getLanguages() {
        return languages;
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement e) {
        if (!e.isValid()) {
            return false;
        }
        return visitInfoForClass(e.getClass()).accept(e);
    }

    @NotNull
    @Override
    public String getElementInfo(@NotNull PsiElement e) {
        return visitInfoForClass(e.getClass()).elementInfo(e);
    }

    @Nullable
    @Override
    public PsiElement getParent(@NotNull PsiElement e) {
        return visitInfoForClass(e.getClass()).parent(e);
    }

    @Nullable
    @Override
    public String getElementTooltip(@NotNull PsiElement e) {
        return visitInfoForClass(e.getClass()).tooltip(e);
    }

    private interface VisitInfo<T extends PsiElement> {
        boolean accept(T e);
        @NotNull
        String elementInfo(T e);
        PsiElement parent(T e);
        @Nullable
        String tooltip(T e);
    }

    private abstract static class ValidBreadcrumb<T extends PsiElement> implements VisitInfo<T> {
        @Override
        public boolean accept(T e) {
            return true;
        }

        @Override
        public PsiElement parent(T e) {
            return e.getParent();
        }

        @Nullable
        @Override
        public String tooltip(T e) {
            return null;
        }
    }

    private static class DefineBreadcrumb extends ValidBreadcrumb<AldorDefine> {
        @NotNull
        @Override
        public String elementInfo(AldorDefine e) {
            Optional<AldorIdentifier> id = e.defineIdentifier();
            return id.map(PsiElement::getText).orElse("<missing>");
        }

        @Nullable
        @Override
        public String tooltip(AldorDefine e) {
            AldorDefine.DefinitionType type = e.definitionType();
            if (type == AldorDefine.DefinitionType.MACRO) {
                return "Macro - " + elementInfo(e);
            }
            Syntax lhs = SyntaxPsiParser.parse(e.lhs());
            return (lhs == null) ? null : SyntaxPrinter.instance().toString(lhs);
        }
    }

    private static class TokenBreadcrumb extends ValidBreadcrumb<PsiElement> {
        private final String name;

        TokenBreadcrumb(String name) {
            this.name = name;
        }
        @NotNull
        @Override
        public String elementInfo(PsiElement e) {
            return name;
        }
    }

    private static class NoBreadcrumbs<T extends PsiElement> implements VisitInfo<T> {

        @Override
        public boolean accept(T e) {
            return false;
        }

        @NotNull
        @Override
        public String elementInfo(T e) {
            throw new IllegalStateException("nope");
        }

        @Override
        public PsiElement parent(T e) {
            return e.getParent();
        }

        @Nullable
        @Override
        public String tooltip(T e) {
            return null;
        }
    }
}
