package aldor.spad;

import aldor.build.AldorCompilationService;
import aldor.file.AldorFileType;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpadLibraryManagerAldorExtendTest {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture codeTestFixture = createFixture(SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule, SdkProjectDescriptors.SourceFileStorageType.Real));
    private final AnnotationFileTestFixture annotationTestFixture= new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTestFixture.rule(codeTestFixture::getProject))
                    .around(JUnits.prePostTestRule(() -> {
                        ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
                        getProject().save();
                    }, () -> ApplicationManagerEx.getApplicationEx().setSaveAllowed(false)));

    private Project getProject() {
        return codeTestFixture.getProject();
    }

    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }


    @Test
    public void testExtension() throws ExecutionException, InterruptedException {
        AldorCompilationService.getAldorCompilationService(getProject());

        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(),
                List.of("base", "extend1", "extend2"),
                ImmutableMap.<String, List<String>>builder()
                    .put("extend1", List.of("base"))
                    .put("extend2", List.of("base", "extend1"))
                .build());
        annotationTestFixture.createFile(getProject(), "Makefile", makefileText);
        String base = "#include \"aldor\"\n" +
                "Foo: with { f: () -> () } == add { f(): () == never }";
        annotationTestFixture.createFile(codeTestFixture.getProject(), "base.as", base);
        String extend1 = "#include \"aldor\"\n" +
                "#library QQ \"base.ao\"\n" +
                "import from QQ;\n" +
                "extend Foo: with { g: () -> () } == add { g(): () == never }";
        annotationTestFixture.createFile(codeTestFixture.getProject(), "extend1.as", extend1);
        String extend2 = "#include \"aldor\"\n" +
                "#library QQ \"extend1.ao\"\n" +
                "import from QQ;\n" +
                "extend Foo: with { h: () -> () } == add { h(): () == never }";
        VirtualFile finalFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "extend2.as", extend2);

        annotationTestFixture.compileFile(finalFile, getProject());

        SpadLibrary mgr = SpadLibraryManager.getInstance(getProject()).forModule(codeTestFixture.getModule(), AldorFileType.INSTANCE);
        assertNotNull(mgr);
        List<Syntax> tt = mgr.allTypes();
        System.out.println("All types: "+ tt);
        assertEquals("[Foo]", tt.toString());

        EdtTestUtil.runInEdtAndWait( () -> System.out.println("Operations: "+  mgr.operations(tt.get(0))));
        EdtTestUtil.runInEdtAndWait( () -> System.out.println("Parents " + mgr.parentCategories(tt.get(0))));
    }

    @Test
    public void testConstructorExtension() throws ExecutionException, InterruptedException {
        AldorCompilationService.getAldorCompilationService(getProject());

        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(),
                List.of("base", "extend1", "extend2"),
                ImmutableMap.<String, List<String>>builder()
                        .put("extend1", List.of("base"))
                        .put("extend2", List.of("base", "extend1"))
                        .build());
        annotationTestFixture.createFile(getProject(), "Makefile", makefileText);
        String base = "#include \"aldor\"\n" +
                "Foo(T: with): with { f: () -> T } == add { f(): T == never }";
        annotationTestFixture.createFile(codeTestFixture.getProject(), "base.as", base);
        String extend1 = "#include \"aldor\"\n" +
                "#library QQ \"base.ao\"\n" +
                "import from QQ;\n" +
                "extend Foo(X: with): with { g: () -> X } == add { g(): X == never }";
        annotationTestFixture.createFile(codeTestFixture.getProject(), "extend1.as", extend1);
        String extend2 = "#include \"aldor\"\n" +
                "#library QQ \"extend1.ao\"\n" +
                "import from QQ;\n" +
                "extend Foo(S: with): with { h: () -> S } == add { h(): S == never }";
        VirtualFile finalFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "extend2.as", extend2);

        annotationTestFixture.compileFile(finalFile, getProject());

        SpadLibrary mgr = SpadLibraryManager.getInstance(getProject()).forModule(codeTestFixture.getModule(), AldorFileType.INSTANCE);
        assertNotNull(mgr);
        List<Syntax> tt = mgr.allTypes();
        System.out.println("All types: "+ tt);
        assertEquals("[Foo]", tt.toString());

        EdtTestUtil.runInEdtAndWait( () -> System.out.println("Operations: "+  mgr.operations(tt.get(0))));
        EdtTestUtil.runInEdtAndWait( () -> System.out.println("Parents " + mgr.parentCategories(tt.get(0))));
    }

    // TODO Note that we don't preserve default values at the moment.. something to fix later
    @Test
    public void testDefaultParameters() throws ExecutionException, InterruptedException {
        AldorCompilationService.getAldorCompilationService(getProject());

        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(),
                List.of("random"),
                Collections.emptyMap());
        annotationTestFixture.createFile(getProject(), "Makefile", makefileText);
        String text = "#include \"aldor\"\n" +
                "Z ==> AldorInteger;\n" +
                "RandomNumbers: with { " +
                "    rgen: (n:Z == 0) -> %;\n" +
                "    rgen: (Z, n:Z == 0) -> %; } == add { rgen(n:Z): % == never;  rgen(m: Z, n:Z): % == never;}";

        VirtualFile file = annotationTestFixture.createFile(codeTestFixture.getProject(), "random.as", text);

        annotationTestFixture.compileFile(file, getProject());

        SpadLibrary mgr = SpadLibraryManager.getInstance(getProject()).forModule(codeTestFixture.getModule(), AldorFileType.INSTANCE);
        assertNotNull(mgr);
        List<Syntax> tt = mgr.allTypes();
        System.out.println("All types: "+ tt);
        assertEquals("[RandomNumbers]", tt.toString());

        EdtTestUtil.runInEdtAndWait( () -> {
            List<SpadLibrary.Operation> operations = mgr.operations(tt.get(0));
            assertEquals(Set.of("rgen"), operations.stream().map(SpadLibrary.Operation::name).collect(Collectors.toSet()));
            assertEquals(Set.of("(AldorInteger, n: AldorInteger) -> %",
                                "(n: AldorInteger) -> %"),
                    operations.stream().map(x -> SyntaxPrinter.instance().toString(x.type())).collect(Collectors.toSet()));

            assertEquals(Set.of(), mgr.parentCategories(tt.get(0)).stream().map(x -> SyntaxPrinter.instance().toString(x)).collect(Collectors.toSet()));
        });
    }


}
