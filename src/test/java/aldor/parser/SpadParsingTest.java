package aldor.parser;

import aldor.file.SpadFileType;
import aldor.lexer.AldorTokenTypes;
import aldor.lexer.LexerFunctions;
import aldor.parser.ParserFunctions.FailReason;
import aldor.psi.elements.AldorTypes;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static aldor.lexer.LexMode.Spad;
import static aldor.parser.ParserFunctions.parseLibrary;
import static aldor.psi.AldorPsiUtils.logPsi;
import static aldor.test_util.TestFiles.existingFile;

public class SpadParsingTest {

    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void testTopLevelStd() {
        PsiElement psi = parseText("++ Some comment\nFoo: with == add\n++ More comment\nQQQ: Category == with\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelSysCmd() {
        PsiElement psi = parseText(")a\nX\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelDecl() {
        PsiElement psi = parseText("++ Some comment\nFoo: with == add\n++ More comment\nB == 2");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelTriv() {
        PsiElement psi = parseText("Foo: with == add");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelDocs() {
        PsiElement psi = parseText("++ Foo\n++More\nFoo: with == add\n++ Bar\n++ More Bar\nBar: with == add\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelAdd() {
        String text = "QQQ(): A == Foo\n add\n   a: B\n";
        System.out.println("Tokens: " + LexerFunctions.tokens(Spad, text).values());
        PsiElement psi = parseText(text);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    @Test
    public void testTopLevelTrivComment() {
        PsiElement psi = parseText("++ Foo\nFoo: with == add");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelSeq() {
        PsiElement psi = parseText("++ Foo\nFoo: with == add\n++ Bar\nBar: with == add\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testTopLevelCategory() {
        PsiElement psi = parseText("QQQ(): Category == X with\n  foo: %a\n add\n  foo: % == 1\n   bar: % == 3\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testDubiousWhereDocco() {
        // See efupxs.spad; no idea where the doc should go in this case.
        PsiElement psi = parseText("++ doc1\nF: _\n Exports == Implementation where\n  ++ doc2\n  foo: F\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testDefine() {
        PsiElement psi = parseText("(x : % - y : %): %", AldorTypes.SPAD_INFIXED);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testFnCall() {
        PsiElement psi = parseText("f x", AldorTypes.SPAD_INFIXED);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testDot() {
        PsiElement psi = parseText("f.x", AldorTypes.SPAD_INFIXED);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    @Test
    public void testBrackets() {
        PsiElement psi = parseText("A := [foo x for x in 1..10]", AldorTypes.SPAD_TOP_LEVEL);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testDefine2() {
        PsiElement psi = parseText("QQQ(): A == add \n (x : % - y : %) : % == x+(-y)");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testDefine3() {
        PsiElement psi = parseText("QQQ(): A == with\n  foo: %\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    @Test
    public void testDefine4() {
        PsiElement psi = parseText("QQQ(): A == with\n  foo: %\n  ++ qq\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWith() {
        PsiElement psi = parseText("with\n  foo: %\n  ++ foo\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWith2() {
        PsiElement psi = parseText("with\n  foo: %\n  ++ foo\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWith3() {
        PsiElement psi = parseText("with\n  foo: %\n  ++ foo\n  ++ more foo\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testDubiousDocco() {
        PsiElement psi = parseText("Foo where \n  ++ this comment must die \n  Something\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testCoerce() {
        PsiElement psi = parseText("(p1 exquo monomial(1, e1))::SUP %");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testUnaryArrow() {
        PsiElement psi = parseText("foo: -> X");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testEndInInfix() {
        PsiElement psi = parseText("fq:= a + foo(a,\n    b) *\n  c\nbar := 2\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testLongLine() {
        PsiElement psi = parseText("add\n  x := [foo\n   ]$List\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testPlusSlash() {
        PsiElement psi = parseText("new(+/[#s for s in l], space$C)\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }



    @Test
    public void testIfStatement() {
        PsiElement psi = parseText("if R has X\n then\n  foo\n else\n  bar\nZZZ\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testIfStatement2() {
        PsiElement psi = parseText("if R has X\nthen\n  foo\nelse\n bar\nZZZ\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testAbbrevAfterIndent() {
        PsiElement psi = parseText("Foo == add\n  blah\n    blah\n)abbrev domain BBB CCC\nBBB == 2");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testIndentedDeclaration() {
        PsiElement psi = parseText("Foo:\n   Category == with\n    XYZ\n");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testBrokenLineDef() {
        String text =
                ")abbrev A B C\n" +
                "++ Foo\n" +
                "FreeAbelianMonoid(S : SetCategory):\n" +
                "  FreeAbelianMonoidCategory(S, NonNegativeInteger)\n" +
                "    == InnerFreeAbelianMonoid(S, NonNegativeInteger, 1)\n";
                PsiElement psi = parseText(text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    @Test
    public void testParseCatDef() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/catdef.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    @Test
    public void testParseCDen() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/cden.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseAggCat() throws IOException {
        Assert.assertNotNull(getProject());
        // Fails: Line 722      "++ to become an in order iterator" seems misplaced
        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/aggcat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseFR() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/fr.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseFMod() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/fmod.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseSuchThat() throws IOException {
        Assert.assertNotNull(getProject());
        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/suchthat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseNumeric() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/numeric.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseAlgFact() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/algfact.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseMultPoly() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/multpoly.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testRadEigen() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/radeigen.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    // FIXME Temp for testing
    @Test
    public void testR() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/r.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseXHash() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/xhash.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParsePlotTool() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/plottool.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testXLPoly() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/xlpoly.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testJet() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/jet.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testLodof() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/lodof.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseOmCat() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/omcat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseFree() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/free.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseString() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/string.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testParseFortran() throws IOException {
        Assert.assertNotNull(getProject());

        File file = existingFile("/home/pab/Work/fricas/fricas/src/algebra/fortran.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    private PsiElement parseFile(File file) throws IOException {
        CharSequence text = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
        //System.out.println(LexerFunctions.tokens(Spad, text).values().stream().map(t -> AldorTokenTypes.isNewLine(t) ? (t + "\n") : (t + " ")).collect(Collectors.joining()));
        return parseText(text);
    }

    // Current fails
    // aggcat: foo ==\n  Join(X)\n add...


    @Test
    public void testAlgebraLibrary() {
        Assert.assertNotNull(getProject());

        File base = existingFile("/home/pab/Work/fricas/fricas/src/algebra");
        Multimap<FailReason, File> badFiles = parseLibrary(getProject(), base, SpadFileType.INSTANCE, Sets.newHashSet(
                "texmacs.spad", // Contains markup
                "unittest.spad", // Contains markup
                "pinterp.spad" // Contains markup
        ));

        for (Map.Entry<FailReason, File> ent: badFiles.entries()) {
            System.out.println("Failed: " + ent.getKey() + " --> " + ent.getValue());
        }
        Assert.assertTrue(badFiles.isEmpty());
    }


    private PsiElement parseText(CharSequence text) {
        //noinspection StringConcatenationMissingWhitespace
        System.out.println(LexerFunctions.tokens(Spad, text).values().stream().map(t -> (t + (AldorTokenTypes.isNewLine(t) ? "\n" : " "))).collect(Collectors.joining()));
        return parseText(text, AldorTypes.SPAD_TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseSpadText(getProject(), text, elementType);
    }

    private Project getProject() {
        return testFixture.getProject();
    }


}
