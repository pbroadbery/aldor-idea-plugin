package aldor.editor.documentation;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * Fixme: Explore ways of adding some documentation.
 * Note that doc strings can be any html
 */
public class AldorDocumentationProvider extends DocumentationProviderEx {
    private static final Logger LOG = Logger.getInstance(AldorDocumentationProvider.class);
    private final TypeAndProviders docTypes = new TypeAndProviders();
    private final DocumentationUtils docUtils = new DocumentationUtils();

    @Override
    @Nullable
    public  String generateDoc(PsiElement element, PsiElement originalElement) {
        LOG.info("Getting documentation for: " + element);
        return docTypes.generateDoc(element, originalElement);
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        //Ctrl-Hover stuff.. should be the type for simple identifiers
        try {
            return docTypes.getQuickNavigateInfo(element, originalElement);
        }
        catch (RuntimeException e) {
            if (!(e instanceof ControlFlowException)) {
                LOG.error("Exception thrown: ", e);
            }
            //noinspection ProhibitedExceptionThrown
            throw e;
        }
    }
}
