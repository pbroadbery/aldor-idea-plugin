package aldor.spad;

import aldor.build.facet.fricas.FricasFacet;
import aldor.lexer.AldorTokenTypes;
import aldor.parser.ParserFunctions;
import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.sdk.SdkTypes;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SkipCI;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasSdkProjectDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FricasSpadLibraryTest {
    private final DirectoryPresentRule directoryPresentRule = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-linux-gnu");
    private final CodeInsightTestFixture testFixture = createFixture(fricasSdkProjectDescriptor(directoryPresentRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directoryPresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.prePostTestRule(this::showSDK, () -> {}))
                    .around(JUnits.setLogToInfoTestRule)
                    .around(JUnits.swingThreadTestRule());

    private void showSDK() {
        Sdk projectSdk = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk();
        System.out.println("SDK IS: "+  projectSdk);
    }

    @Test
    public void testParents0() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder().project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();
        Syntax syntax = Id.createMissingId(AldorTokenTypes.TK_Id, "Integer");

        List<Syntax> pp = lib.parentCategories(syntax);
        for (Syntax p: pp) {
            System.out.println("Parent category: " + p);
        }
        assertFalse(pp.isEmpty());
        lib.dispose();
    }

    @Test
    @SkipCI
    public void testOperations() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder().project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Collection<AldorDefine> ll = AldorDefineTopLevelIndex.instance.get("Group", testFixture.getProject(), GlobalSearchScope.allScope(testFixture.getProject()));

        Syntax syntax = Objects.requireNonNull(SyntaxPsiParser.parse(ll.iterator().next().lhs())).as(DeclareNode.class).lhs();
        System.out.println("Syntax is " + syntax);
        List<SpadLibrary.Operation> pp = lib.operations(syntax);
        for (SpadLibrary.Operation p: pp) {
            System.out.println("Operation: " + p + " "+ p.declaration());
        }

        pp.forEach(op -> assertNotNull(op.declaration()));

        assertFalse(pp.isEmpty());
        List<PsiElement> decls = pp.stream().map(SpadLibrary.Operation::declaration).collect(Collectors.toList());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("commutator")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("conjugate")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("inv")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("^")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("/")).count());
        lib.dispose();
    }

    @Test
    @SkipCI
    public void testCoercibleToOperations() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "CoercibleTo OutputForm");

        System.out.println("Syntax is " + syntax);
        List<SpadLibrary.Operation> pp = lib.operations(syntax);
        for (SpadLibrary.Operation p: pp) {
            System.out.println("Operation: " + p + " "+ p.type());
            System.out.println("Operation: " + p + " "+ p.declaration());
        }
        assertFalse(pp.isEmpty());
        pp.forEach(op -> assertNotNull(op.declaration()));

        assertEquals(1, pp.size());
        assertEquals("coerce", pp.get(0).name());
        assertEquals("(Apply -> % OutputForm)", pp.get(0).type().toString());
        lib.dispose();
    }

    @Test
    @SkipCI
    public void testFacetCategory() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "FacetCategory");

        System.out.println("Syntax is " + syntax);
        List<SpadLibrary.Operation> pp = lib.operations(syntax);
        for (SpadLibrary.Operation p: pp) {
            System.out.println("Operation: " + p + " "+ p.type());
            System.out.println("Operation: " + p + " "+ p.declaration());
            System.out.println("Operation: " + p + " "+ p.implementation());
        }
        assertFalse(pp.isEmpty());
        //pp.forEach(op -> assertNotNull(op.declaration())); // ideally, but macros in the source get in the way

        assertEquals(3, pp.size());
        Map<String, SpadLibrary.Operation> opForName = pp.stream().collect(Collectors.toMap(SpadLibrary.Operation::name, x -> x));
        assertEquals(3, opForName.size());

        for (String name: new String[]{"empty", ""}) {
            SpadLibrary.Operation op = opForName.get("getMult");
            assertNotNull(op.declaration());
        }

        lib.dispose();
    }


    @Test
    public void testListAggregrateRec() {
        JUnits.setLogToInfo();
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "ListAggregate X");

        List<Syntax> parents = lib.parentCategories(syntax);
        for (Syntax parent: parents) {
            List<SpadLibrary.Operation> pp = lib.operations(parent);
            for (SpadLibrary.Operation p: pp) {
                System.out.println("Operation: " + p + " "+ p.declaration());
            }
        }
        assertNotNull(parents);
        lib.dispose();
    }

    @Test
    public void testLeftModuleConditions() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "LeftModule X");
        List<Syntax> parents = lib.parentCategories(syntax);

        parents.forEach(p -> System.out.println("Parent: " + p));

        assertEquals(new HashSet<>(
                        Arrays.asList(
                                "(If (Apply has X AbelianGroup) AbelianGroup)",
                                "(If (Apply has X AbelianMonoid) AbelianMonoid)",
                                "AbelianSemiGroup")),
                parents.stream().map(Object::toString).collect(Collectors.toSet()));
    }

    @Test
    @SkipCI
    public void testLeftModuleOperations() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "LeftModule NotNegativeInteger");
        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> allparents = lib.allParents(syntax);
        List<SpadLibrary.ParentType> parents = allparents.first;
        List<SpadLibrary.Operation> operations = allparents.second;

        assertEquals(new HashSet<>(
                        Arrays.asList(
                                "CancellationAbelianMonoid",
                                "BasicType",
                                "AbelianSemiGroup",
                                "AbelianGroup",
                                "(Apply LeftModule NotNegativeInteger)",
                                "AbelianMonoid",
                                "SetCategory",
                                "(Apply CoercibleTo OutputForm)")),
                parents.stream().map(p -> p.type().toString()).collect(Collectors.toSet()));

        Map<String, List<SpadLibrary.Operation>> map = new HashMap<>();
        for (SpadLibrary.Operation op: operations) {
            map.computeIfAbsent(op.name(), n -> new ArrayList<>()).add(op);
        }

        List<SpadLibrary.Operation> coerces = map.get("coerce");
        assertNotNull(coerces);
        assertEquals(1, coerces.size());
        SpadLibrary.Operation coerce = coerces.get(0);
        assertNotNull(coerce.declaration());

        PsiElement decl = coerce.declaration();
        assertNotNull(decl);
        assertEquals("coerce.spad", decl.getContainingFile().getName());
    }

    @Test
    @SkipCI
    public void testPolyIntOperations() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "Polynomial Integer");
        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> allparents = lib.allParents(syntax);
        List<SpadLibrary.ParentType> parents = allparents.first;
        List<SpadLibrary.Operation> operations = allparents.second;
    }

    @Test
    @SkipCI
    public void testAbelianGroup() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "AbelianGroup");
        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> allparents = lib.allParents(syntax);
        List<SpadLibrary.ParentType> parents = allparents.first;
        List<SpadLibrary.Operation> operations = allparents.second;

        parents.forEach(p -> System.out.println("parent " + p.type() + " " + p.exporter()));
        operations.forEach(op -> System.out.println("operation " + op.name() + ": " + op.type() + "\t\t" +
                op.containingForm().getText().substring(0, 20)));
    }


    @Test
    @SkipCI
    public void testSUPXIntOperations() {
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "SparseUnivariatePolynomial X");
        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> allparents = lib.allParents(syntax);
        List<SpadLibrary.ParentType> parents = allparents.first;
        List<SpadLibrary.Operation> operations = allparents.second;
    }

    @NotNull
    private VirtualFile projectSdkAlgebraDirectory() {
        Sdk projectSdk = FricasFacet.forModule(testFixture.getModule()).getConfiguration().sdk();
        assertNotNull(projectSdk);
        VirtualFile dir = SdkTypes.algebraPath(projectSdk);
        assertNotNull(dir);
        return dir;
    }

}
