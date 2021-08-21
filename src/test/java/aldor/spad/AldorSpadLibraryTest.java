package aldor.spad;

import aldor.parser.ParserFunctions;
import aldor.syntax.Syntax;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.PathBasedTestRule;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.aldorSdkProjectDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AldorSpadLibraryTest {
    private final PathBasedTestRule executablePresentRule = new ExecutablePresentRule.AldorStd();
    private final CodeInsightTestFixture testFixture = createFixture(aldorSdkProjectDescriptor(executablePresentRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(executablePresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.prePostTestRule(this::showSDK, () -> {}))
                    .around(JUnits.setLogToInfoTestRule)
                    .around(JUnits.swingThreadTestRule());

    private void showSDK() {
        Sdk projectSdk = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk();
        System.out.println("SDK IS: "+  projectSdk);
    }

    @Test
    public void testPrimitiveTypeOperations() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(), VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                .build();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "PrimitiveType");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);
        assertEquals(2, ops.size());
        ops.forEach(p -> assertNotNull(p.declaration()));
    }

    @Test
    public void testIndexedAtomOperations() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(),
                                VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                            .build();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "IndexedAtom");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);

        assertEquals(List.of("atom", "index", "isNegation?", "negate", "negated?", "positive"),
                ops.stream().map(op -> op.name()).sorted().collect(Collectors.toList()));
        ops.forEach(p -> System.out.println("Parent: " + p.declaration()));
    }

    @Test
    public void testRandomNumberGenerator() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(),
                VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                .build();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "RandomNumberGenerator");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);

        Map<String, List<SpadLibrary.Operation>> opsForName = ops.stream().collect(Collectors.groupingBy(op -> op.name()));

        SpadLibrary.Operation apply = opsForName.get("apply").get(0);
        assertNotNull(apply.declaration());
    }


    @Test
    public void testListString() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(),
                VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                .build();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "List String");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);

        Map<String, List<SpadLibrary.Operation>> opsForName = ops.stream().collect(Collectors.groupingBy(op -> op.name()));
        opsForName.forEach( (k, v) -> {
            System.out.println("Operations " + k + " --> " + v);
                });
        SpadLibrary.Operation apply = opsForName.get("apply").get(0);
        assertNotNull(apply.declaration());
    }
}
