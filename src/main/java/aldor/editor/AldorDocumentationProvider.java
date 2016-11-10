package aldor.editor;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
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
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(element.getProject());
        Optional<Pair<Module, VirtualFile>> moduleMaybe = moduleManager.aldorModuleForFile(virtualFile);
        LOG.info("Module: " + element.getContainingFile().getName() + " " + moduleMaybe);
        if (!moduleMaybe.isPresent()) {
            return null;
        }
        if (!isInterestingElement(element)) {
            return null;
        }
        Module module = moduleMaybe.get().first;
        AnnotationFileManager annotationManager = AnnotationFileManager.getAnnotationFileManager(module);
        assert annotationManager != null;
        AnnotationFile file = annotationManager.annotationFile(element.getContainingFile());
        if (file.errorMessage() != null) {
            return "Error when reading file: " + file.errorMessage();
        }
        SrcPos srcPos = annotationManager.findSrcPosForElement(element);
        LOG.info("Srcpos for id is: " + srcPos + " " + element.getText());
        Syme syme = file.lookupSyme(srcPos);
        if (syme == null) {
            return "no information";
        }

        SyntaxPrinter printer = SyntaxPrinter.instance();
        Syntax exporter = syme.exporter();
        Syntax type = syme.type();

        return "<p><b>exporter:</b> " + printer.toString(exporter) + "</b></p>" + "\n<p><b>type:</b> " + printer.toString(type) + "</p>";
    }

    private boolean isInterestingElement(PsiElement element) {
        return element instanceof AldorIdentifier;
    }

}
