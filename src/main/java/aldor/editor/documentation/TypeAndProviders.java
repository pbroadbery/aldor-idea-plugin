package aldor.editor.documentation;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.SpadAbbrev;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("NonConstantFieldWithUpperCaseName")
class TypeAndProviders {
    private static final Logger LOG = Logger.getInstance(TypeAndProviders.class);

    private final ConcurrentMap<Class<? extends PsiElement>, TypeAndProvider<?>> lookup = new ConcurrentHashMap<>();
    private final TypeAndProvider<AldorIdentifier> IDENTIFIER = new TypeAndProvider<>(AldorIdentifier.class, new IdentifierDocumentationProvider());
    private final TypeAndProvider<AldorDefine> DEFINE = new TypeAndProvider<>(AldorDefine.class, new DefineDocumentationProvider());
    private final TypeAndProvider<AldorDeclare> DECLARE = new TypeAndProvider<>(AldorDeclare.class, new DeclareDocumentationProvider());
    private final TypeAndProvider<SpadAbbrev> SPADABBREV = new TypeAndProvider<>(SpadAbbrev.class, new SpadAbbrevDocumentationProvider());
    private final TypeAndProvider<PsiElement> MISSING = new TypeAndProvider<>(PsiElement.class, new TypedDocumentationProvider<>());
    private final List<TypeAndProvider<?>> all = Arrays.asList(IDENTIFIER, DEFINE, DECLARE, SPADABBREV, MISSING);

    <T extends PsiElement> TypedDocumentationProvider<T> providerForElement(T elt) {
        @SuppressWarnings("unchecked")
        TypeAndProvider<T> docType = (TypeAndProvider<T>) lookup.computeIfAbsent(elt.getClass(), k -> findDocTypeForClass(elt.getClass()));
        return docType.instance();
    }

    @SuppressWarnings("unchecked")
    private <T extends PsiElement> TypeAndProvider<? super T> findDocTypeForClass(Class<T> aClass) {
        for (TypeAndProvider<?> type : all) {
            if (type.clzz().isAssignableFrom(aClass)) {
                return (TypeAndProvider<T>) type;
            }
        }
        return MISSING;
    }

    public String generateDoc(PsiElement element, PsiElement originalElement) {
        return providerForElement(element).generateDoc(element, originalElement);
    }

    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return providerForElement(element).getQuickNavigateInfo(element, originalElement);
    }
}
