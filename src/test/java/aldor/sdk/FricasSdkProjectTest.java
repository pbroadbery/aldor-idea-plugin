package aldor.sdk;

import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDefine;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FricasSdkProjectTest {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(fricasExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void test() {
        // Not an especially good test, just shows parsing happened, and we can find some docs
        Project project = codeTestFixture.getProject();
        Collection<VirtualFile> allFiles = FilenameIndex.getAllFilesByExt(project, "spad",
                GlobalSearchScope.moduleWithLibrariesScope(codeTestFixture.getModule()));
        Sdk sdk = ModuleRootManager.getInstance(codeTestFixture.getModule()).getSdk();
        PsiFile[] files = FilenameIndex.getFilesByName(project, "catdef.spad", GlobalSearchScope.allScope(project));

        Assert.assertEquals(1, files.length);
        PsiFile theFile = files[0];
        Collection<AldorDefine> definitions = PsiTreeUtil.findChildrenOfType(theFile, AldorDefine.class);
        List<AldorDefine> defs = definitions.stream()
                                            .filter(def -> def.definitionType() == AldorDefine.DefinitionType.CONSTANT)
                                            .collect(Collectors.toList());
        List<AldorDefine> oringDefinitions = defs.stream().filter(def -> def.defineIdentifier().map(id -> "OrderedRing".equals(id.getText())).orElse(false)).
                collect(Collectors.toList());


        Assert.assertEquals(1, oringDefinitions.size());

        AldorDefine def = oringDefinitions.get(0);

        String doc = docForElement(def);
        assertNotNull(doc);
        assertTrue(doc.contains("Ordered sets"));
    }

    @Nullable
    private String docForElement(PsiElement id) {
        if (id == null) {
            return null;
        }
        return DocumentationManager.getProviderFromElement(id).generateDoc(id, id);
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule.prefix());

    }

}
