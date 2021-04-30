package aldor.spad;

import aldor.lexer.AldorTokenTypes;
import aldor.syntax.Syntax;
import aldor.syntax.components.Id;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.List;
import java.util.Objects;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasLocalSdkProjectDescriptor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FricasLocalSpadLibraryTest {

    private final DirectoryPresentRule directoryPresentRule = new DirectoryPresentRule("/home/pab/tmp/plugin/test/fricas_git");
    private final CodeInsightTestFixture testFixture = createFixture(fricasLocalSdkProjectDescriptor(directoryPresentRule.path()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directoryPresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testParents0() {
        CompilerModuleExtension instance = CompilerModuleExtension.getInstance(testFixture.getModule());
        assertNotNull(instance);
        VirtualFile baseBuildPath = instance.getCompilerOutputPath();
        assertNotNull(baseBuildPath);
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .nrlibDirectory(Objects.requireNonNull(baseBuildPath.findFileByRelativePath("src/algebra")),
                                baseBuildPath.findFileByRelativePath("../fricas/src/algebra"))
                .createFricasSpadLibrary();

        System.out.println("All types: " + lib.allTypes());

        Syntax syntax = Id.createMissingId(AldorTokenTypes.TK_Id, "Ring");

        List<Syntax> pp = lib.parentCategories(lib.normalise(syntax));
        for (Syntax p : pp) {
            System.out.println("Parent category: " + p);
        }
        assertFalse(pp.isEmpty());
        lib.dispose();
    }


}
