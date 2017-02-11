package aldor.editor;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorDocumented;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.symbolfile.Syme;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.util.AnnotatedOptional;
import com.google.common.base.Joiner;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static aldor.util.AnnotatedOptional.missing;

/**

 Fixme: Explore ways of adding some documentation.
 * Note that doc strings can be any html
 * <b>this is bold</b>
 *
 *
 */
public class AldorDocumentationProvider extends DocumentationProviderEx {
    private static final Logger LOG = Logger.getInstance(AldorDocumentationProvider.class);
    private static final Pattern NewLine = Pattern.compile("\n", Pattern.LITERAL);

    @Override
    @Nullable
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!isInterestingElement(element)) {
            return null;
        }

        AnnotatedOptional<Syme, String> symeMaybe = symeForElement(element);

        if (symeMaybe.isPresent()) {
            SyntaxPrinter printer = SyntaxPrinter.instance();
            Syme syme = symeMaybe.get();
            Syntax exporter = syme.exporter();
            Syntax type = syme.type();

            String typeText = "<p><b>exporter:</b> " + printer.toString(exporter) + "</b></p>" + "\n<p><b>type:</b> " + printer.toString(type) + "</p>";

            String docString = aldorDocString(syme, element, originalElement);
            if (docString == null) {
                docString = aldorDocStringFromIndex(element, originalElement);
            }
            return typeText + "<code>" + docString + "</code>";
        }
        else {
            String docString = "File lookup failed: " + symeMaybe.failInfo();
            String indexDoc = aldorDocStringFromIndex(element, originalElement);
            if (indexDoc != null) {
                docString = docString + "<hr>\n" + indexDoc;
            }
            return docString;
        }
    }

    private String aldorDocStringFromIndex(PsiElement element, PsiElement originalElement) {
        AldorDefine definitionForName = findTopLevelDefine(element);
        if (definitionForName == null) {
            return null;
        }
        AldorDocumented documented = (AldorDocumented) PsiTreeUtil.findFirstParent(definitionForName, elt -> elt instanceof AldorDocumented);

        return docString(documented);
    }

    private AldorDefine findTopLevelDefine(PsiElement element) {
        // Maybe this should use references
        Project project = element.getProject();
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get(element.getText(), project, GlobalSearchScope.allScope(project));

        if (items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            return items.iterator().next();
        }
        return items.iterator().next();
    }

    private String aldorDocString(Syme syme, PsiElement element, PsiElement originalElement) {
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(element.getProject());
        AnnotatedOptional<AnnotationFileManager, String> annotationManager = moduleManager.annotationFileManagerForFile(element.getProject(), element.getContainingFile().getVirtualFile());
        if (!annotationManager.isPresent()) {
            return annotationManager.failInfo();
        }
        PsiElement resolved = annotationManager.get().lookupReference(element);
        AldorDocumented documented = (AldorDocumented) PsiTreeUtil.findFirstParent(resolved, elt -> elt instanceof AldorDocumented);
        return docString(documented);
    }

    @Nullable
    private String docString(AldorDocumented documented) {
        if (documented == null) {
            return null;
        }
        return Joiner.on("<br/>\n").join(documented.documentationNodes().stream().map((psiElement) -> NewLine.matcher(psiElement.getText()).replaceAll(Matcher.quoteReplacement("<br/>\n"))).collect(Collectors.toList()));
    }

    AnnotatedOptional<Syme, String> symeForElement(PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        Project project = element.getProject();
        if (virtualFile == null) {
            return missing("No file for element");
        }
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(project);
        AnnotatedOptional<AnnotationFileManager, String> annotationManager = moduleManager.annotationFileManagerForFile(project, virtualFile);

        return annotationManager.flatMap(am -> am.symeForElement(element));

    }

    private boolean isInterestingElement(PsiElement element) {
        return element instanceof AldorIdentifier;
    }
}
