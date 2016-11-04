package aldor.parser;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static aldor.parser.ParserFunctions.parseLibrary;
import static aldor.psi.AldorPsiUtils.logPsi;

/**
 * Lexer Test. Created by pab on 30/08/16.
 */
@SuppressWarnings({"HardCodedStringLiteral", "ClassWithTooManyMethods"})
public class EnsureParsingTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new AldorProjectDescriptor();
    }

    public void testParseCatDefinition() {
        String text = "X: with == add";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseCommentDefinitions() {
        String text = "X: with == add; Y: with == add";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseMap() {
        String text = "((A: Tuple Type) -> (R: Tuple Type)): with == add;";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseWithBlock() {
        String text = "QQ: with { f: % } == add";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseAddBlock() {
        String text = "QQ: with { f: % } == add { f: % == 23 }";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseError() {
        String text = ":= 2";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(1, errors.size());
    }


    public void testParseAddBlock2() {
        String text = "QQ: with { f: % } == XX add { f: % == 23 }";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }


    public void testParseDefn() {
        String text = "f(x: Z): Z == never";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }


    public void testParseFnDefn() {
        String text = "f(x: Z): Z == 1";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testNegateFnDefn() {
        String text = "-(x: Z): Z == 1";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testFn() {
        String text = "f x";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testNegate() {
        String text = "-1";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testParseNothing() {
        String text = "with { f: () -> () }";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseApplication() {
        String text = "f x";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseApplication2() {
        String text = "f g x";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseSequence() {
        String text = "with { f: X; g: Y }";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseDomain() {
        String text = "A: with { f: X; g: BInt -> () } == add { dispose!(x: %): () == never }";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testDeclInfix() {
        String text = "+: %";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testDefSeq() {
        String text = "a == add {} b == add {}";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testIf() {
        String text = "if foo then 1 else 2";
        PsiElement psi = parseText(text, AldorTypes.IF_STATEMENT_BAL_STATEMENT);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testDefine() {
        String text = "foo: X == 2";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testGenerator() {
        String text = "f(x for x in 1..10)";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testGenerator2() {
        String text = "x for x in 1..10";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testWithSysCmd() {
        String text = "foo\n#Wibble\nbar";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testWithIfBlock() {
        String text = "a;\n#if X\nstuff\n#endif\nb\n";
        PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testEmpty() {
        ParserDefinition aldorParserDefinition = new AldorParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(aldorParserDefinition, aldorParserDefinition.createLexer(null),
                "");

        PsiParser parser = aldorParserDefinition.createParser(getProject());
        ASTNode parsed = parser.parse(AldorTypes.PRE_DOCUMENT, psiBuilder);

        logPsi(parsed.getPsi());
        assertTrue(ParserFunctions.getPsiErrorElements(parsed.getPsi()).isEmpty());
    }

    public void testParseLang() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/lang/sal_lang.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseITools() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/arith/sal_itools.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseSalSSet() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/datastruc/sal_sset.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseFold() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/datastruc/sal_fold.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseBSearch() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/arith/sal_bsearch.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseUPMod() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/algebra/src/algext/sit_upmod.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseSExpr() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/lisp/sal_sexpr.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    public void testParseAxiomPrime() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/algebra/src/categories/sit_axiomprime.as");
        final List<PsiErrorElement> errors = parseFile(project, file);
        assertEquals(0, errors.size());
    }

    @NotNull
    private List<PsiErrorElement> parseFile(Project project, File file) {
        assertTrue(file.exists());
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
        assertNotNull(vf);
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
        //noinspection ConstantConditions
        String text = psiFile.getText();

        PsiElement psi = parseText(text);

        return ParserFunctions.getPsiErrorElements(psi);
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text, AldorTypes.TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType eltType) {
        return ParserFunctions.parseAldorText(getProject(), text, eltType);
    }

    public void testAldorLibrary() {
        assertNotNull(getProject());

        File base = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src");
        Multimap<ParserFunctions.FailReason, File> badFiles = parseLibrary(getProject(), base, Sets.newHashSet());

        for (Map.Entry<ParserFunctions.FailReason, File> ent: badFiles.entries()) {
            System.out.println("Failed: " + ent.getKey() + " --> " + ent.getValue());
        }
        assertTrue(badFiles.isEmpty());
    }


    public void testAlgebraLibrary() {
        assertNotNull(getProject());

        File base = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/algebra/src");
        Set<String> blackList = Sets.newHashSet("tst_dup.as", "tst_fold.as",
                "sit_upolc0.as", "sit_upolc.as");
        Multimap<ParserFunctions.FailReason, File> badFiles = parseLibrary(getProject(), base, blackList);

        for (Map.Entry<ParserFunctions.FailReason, File> ent: badFiles.entries()) {
            System.out.println("Failed: " + ent.getKey() + " --> " + ent.getValue());
        }
        assertTrue(badFiles.isEmpty());
    }

    public static class AldorProjectDescriptor extends LightProjectDescriptor {

    }


}
