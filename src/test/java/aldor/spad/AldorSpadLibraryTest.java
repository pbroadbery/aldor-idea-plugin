package aldor.spad;

import aldor.parser.ParserFunctions;
import aldor.syntax.Syntax;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.PathBasedTestRule;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.testFramework.TestLoggerFactory;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static aldor.test_util.JUnits.prePostTestRule;
import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.aldorSdkProjectDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorSpadLibraryTest {
    private final PathBasedTestRule executablePresentRule = new ExecutablePresentRule.AldorStd();
    private final CodeInsightTestFixture testFixture = createFixture(aldorSdkProjectDescriptor(executablePresentRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(TestLoggerFactory.createTestWatcher())
                    .around(executablePresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(prePostTestRule(this::showSDK, () -> {}))
                    .around(JUnits.setLogToInfoTestRule)
                    .around(JUnits.swingThreadTestRule());

    private void showSDK() {
        Sdk projectSdk = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk();
        System.out.println("SDK IS: "+  projectSdk);
    }

    @Test
    public void testType() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(), VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                .build();
        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "Type");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);
        assertEquals(0, ops.size());
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

        assertEquals(List.of("atom", "index", "isNegation?"),
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

    @SuppressWarnings("MagicNumber")
    @Test
    public void testListString() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(),
                VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                .build();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "List String");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);
        System.out.println("Found " + ops.size() + " operations");
        assertFalse(ops.isEmpty());
        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> pp = lib.allParents(syntax);
        // Random numbers... as long as we get *something*, we probably have everything
        assertTrue(pp.getSecond().size() > 20);
        assertTrue(pp.getFirst().size() > 10);
        System.out.println("Ops: " + pp.getSecond().size());
        for (var op: pp.getSecond()) {
            System.out.println("Operation: "+ op);
        }
    }

    @Test
    public void testUndefined() {
        SpadLibrary lib = new AldorSdkSpadLibraryBuilder(testFixture.getProject(),
                VfsUtil.findFile(Path.of(executablePresentRule.path()), false))
                .build();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "FooBar String");
        List<SpadLibrary.Operation> ops = lib.operations(syntax);
        System.out.println("Found " + ops.size() + " operations");
        assertTrue(ops.isEmpty());
        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> pp = lib.allParents(syntax);
        assertEquals(1, pp.getFirst().size());
        assertEquals("*Unknown*", pp.getFirst().get(0).type().toString());
        assertTrue(pp.getSecond().isEmpty());
    }
}
