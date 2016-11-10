package aldor.editor;

import aldor.build.module.AldorModuleType;
import aldor.psi.AldorIdentifier;
import aldor.util.SExpression;
import aldor.util.SymbolPolicy;
import aldor.util.VirtualFileTests;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

import static aldor.symbolfile.AnnotationFileTests.name;
import static aldor.symbolfile.AnnotationFileTests.srcpos;
import static aldor.symbolfile.AnnotationFileTests.syme;
import static aldor.symbolfile.AnnotationFileTests.type;
import static aldor.symbolfile.SymbolFileSymbols.Id;
import static aldor.util.SExpressions.list;
import static aldor.util.VirtualFileTests.createFile;

public class AldorDocumentationProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testDocProvider() throws IOException {
        VirtualFile root = VirtualFileTests.getProjectRoot(getProject());

        VirtualFile virtualFile = createFile(root, "foo.as", "a; b; c; d;");
        createFile(root, "foo.abn", createMockTypeMarkup().toString(SymbolPolicy.ALLCAPS));
        PsiFile file = getPsiManager().findFile(virtualFile);

        Collection<AldorIdentifier> ids = PsiTreeUtil.findChildrenOfType(file, AldorIdentifier.class);

        for (AldorIdentifier id : ids) {
            assertNotNull(id.getContainingFile());
            String docco = docForElement(id);
            System.out.println("Doc is: " + docco);
        }

        String firstDoc = docForElement(ids.iterator().next());
        assertTrue(firstDoc.contains("> T<"));
    }

    private String docForElement(PsiElement id) {
        return DocumentationManager.getProviderFromElement(id).generateDoc(id, null);
    }

    private SExpression createMockTypeMarkup() {
        String file = "foo";
        return list(
                list(//Syntax
                        list(Id, srcpos(file, 1, 1), syme(0)),
                        list(Id, srcpos(file, 1, 4), syme(1)),
                        list(Id, srcpos(file, 1, 7), syme(2))),
                list(//Symbols
                        list(name("a"), type(0)),
                        list(name("b"), type(1)),
                        list(name("c"), type(1)),
                        list(name("T"))),
                list(//Types
                        list(Id, syme(3)),
                        list(Id, syme(3))));

    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        //noinspection ReturnOfInnerClass
        return new LightProjectDescriptor() {

            @Override
            @NotNull
            public ModuleType<?> getModuleType() {
                return AldorModuleType.instance();
            }

        };
    }
}
