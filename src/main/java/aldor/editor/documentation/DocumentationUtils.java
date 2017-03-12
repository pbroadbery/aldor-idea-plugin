package aldor.editor.documentation;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorDefine;
import aldor.psi.AldorDocumented;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.symbolfile.Syme;
import aldor.util.AnnotatedOptional;
import com.google.common.base.Joiner;
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

public class DocumentationUtils {
    private static final Logger LOG = Logger.getInstance(DocumentationUtils.class);
    private static final Pattern NewLine = Pattern.compile("\n", Pattern.LITERAL);

    @Nullable
    public String aldorDocStringFromIndex(PsiElement element, PsiElement originalElement) {
        AldorDefine definitionForName = findTopLevelDefine(element);
        if (definitionForName == null) {
            return null;
        }
        AldorDocumented documented = (AldorDocumented) PsiTreeUtil.findFirstParent(definitionForName, elt -> elt instanceof AldorDocumented);

        return docString(documented);
    }

    @Nullable
    public String aldorDocStringFromContainingElement(PsiElement element) {
        AldorDocumented documented = (AldorDocumented) PsiTreeUtil.findFirstParent(element, elt -> elt instanceof AldorDocumented);

        return docString(documented);
    }

    @Nullable
    public AldorDefine findTopLevelDefine(PsiElement element) {
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

    public String aldorDocString(Syme syme, PsiElement element, PsiElement originalElement) {
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(element.getProject());
        AnnotatedOptional<AnnotationFileManager, String> annotationManager = moduleManager.annotationFileManagerForFile(element.getContainingFile().getVirtualFile());
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
        AnnotatedOptional<AnnotationFileManager, String> annotationManager = moduleManager.annotationFileManagerForFile(virtualFile);

        return annotationManager.flatMap(am -> am.symeForElement(element));

    }

}
