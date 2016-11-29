package aldor.editor;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.util.AnnotatedOptional;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**

 Fixme: Explore ways of adding some documentation.
 * Note that doc strings can be any html
 * <b>this is bold</b>
 *
 *
 */
public class AldorDocumentationProvider extends DocumentationProviderEx {
    private static final Logger LOG = Logger.getInstance(AldorDocumentationProvider.class);

    @Override
    @Nullable
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!isInterestingElement(element)) {
            return null;
        }

        AnnotatedOptional<Syme, String> symeMaybe = symeForElement(originalElement);

        String typeText = "";
        if (symeMaybe.isPresent()) {
            SyntaxPrinter printer = SyntaxPrinter.instance();
            Syntax exporter = symeMaybe.get().exporter();
            Syntax type = symeMaybe.get().type();

            typeText = "<p><b>exporter:</b> " + printer.toString(exporter) + "</b></p>" + "\n<p><b>type:</b> " + printer.toString(type) + "</p>";
        }

        /*
        if (element instanceof AldorIdentifier) {
            AldorIdentifier id = (AldorIdentifier) element;
            PsiReference ref = id.getReference();
            PsiElement resolution = ref.resolve();
            String docco = AldorPsiUtils.documentationForId(resolution);
        }
        */

        return typeText;
    }

    AnnotatedOptional<Syme, String> symeForElement(PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return AnnotatedOptional.missing("No file for element(!)");
        }
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(element.getProject());
        Optional<Module> moduleMaybe = moduleManager.aldorModuleForFile(virtualFile);
        LOG.info("Module: " + element.getContainingFile().getName() + " " + moduleMaybe);
        if (!moduleMaybe.isPresent()) {
            return AnnotatedOptional.missing("No module for file " + virtualFile.getName());
        }
        Module module = moduleMaybe.get();
        AnnotationFileManager annotationManager = AnnotationFileManager.getAnnotationFileManager(module);
        assert annotationManager != null;
        AnnotationFile file = annotationManager.annotationFile(element.getContainingFile());
        if (file.errorMessage() != null) {
            return AnnotatedOptional.missing("Error when reading file: " + file.errorMessage());
        }
        SrcPos srcPos = annotationManager.findSrcPosForElement(element);
        LOG.info("Srcpos for id is: " + srcPos + " " + element.getText());
        Syme syme = file.lookupSyme(srcPos);
        if (syme == null) {
            return AnnotatedOptional.missing("no information");
        }

        return AnnotatedOptional.of(syme);
    }


    private boolean isInterestingElement(PsiElement element) {
        return element instanceof AldorIdentifier;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return super.getDocumentationElementForLink(psiManager, link, context);
    }
}
