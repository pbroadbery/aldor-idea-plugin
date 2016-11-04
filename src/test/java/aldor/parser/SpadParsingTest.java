package aldor.parser;

import aldor.lexer.AldorTokenTypes;
import aldor.lexer.LexerFunctions;
import aldor.parser.ParserFunctions.FailReason;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

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

public class SpadParsingTest extends LightPlatformCodeInsightTestCase {

    public void testTopLevelStd() {
        PsiElement psi = parseText("++ Some comment\nFoo: with == add\n++ More comment\nQQQ: Category == with\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testTopLevelSysCmd() {
        PsiElement psi = parseText(")a\nX\n");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testTopLevelDecl() {
        PsiElement psi = parseText("++ Some comment\nFoo: with == add\n++ More comment\nB == 2");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testTopLevelTriv() {
        PsiElement psi = parseText("Foo: with == add");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testTopLevelDocs() {
        PsiElement psi = parseText("++ Foo\n++More\nFoo: with == add\n++ Bar\n++ More Bar\nBar: with == add\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testTopLevelAdd() {
        String text = "QQQ(): A == Foo\n add\n   a: B\n";
        System.out.println("Tokens: " + LexerFunctions.tokens(Spad, text).values());
        PsiElement psi = parseText(text);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testTopLevelTrivComment() {
        PsiElement psi = parseText("++ Foo\nFoo: with == add");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testTopLevelCategory() {
        PsiElement psi = parseText("QQQ(): Category == X with\n  foo: %a\n add\n  foo: % == 1\n   bar: % == 3\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testDubiousWhereDocco() {
        // See efupxs.spad; no idea where the doc should go in this case.
        PsiElement psi = parseText("++ doc1\nF: _\n Exports == Implementation where\n  ++ doc2\n  foo: F\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testDefine() {
        PsiElement psi = parseText("(x : % - y : %): %", AldorTypes.SPAD_INFIXED);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testFnCall() {
        PsiElement psi = parseText("f x", AldorTypes.SPAD_INFIXED);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testDot() {
        PsiElement psi = parseText("f.x", AldorTypes.SPAD_INFIXED);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testBrackets() {
        PsiElement psi = parseText("A := [foo x for x in 1..10]", AldorTypes.SPAD_TOP_LEVEL);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }



    public void testDefine2() {
        PsiElement psi = parseText("QQQ(): A == add \n (x : % - y : %) : % == x+(-y)");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testDefine3() {
        PsiElement psi = parseText("QQQ(): A == with\n  foo: %\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testDefine4() {
        PsiElement psi = parseText("QQQ(): A == with\n  foo: %\n  ++ qq\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testWith() {
        PsiElement psi = parseText("with\n  foo: %\n  ++ foo\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testWith2() {
        PsiElement psi = parseText("with\n  foo: %\n  ++ foo\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testWith3() {
        PsiElement psi = parseText("with\n  foo: %\n  ++ foo\n  ++ more foo\n  bar: %\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testCoerce() {
        PsiElement psi = parseText("(p1 exquo monomial(1, e1))::SUP %");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testUnaryArrow() {
        PsiElement psi = parseText("foo: -> X");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testEndInInfix() {
        PsiElement psi = parseText("fq:= a + foo(a,\n    b) *\n  c\nbar := 2\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testLongLine() {
        PsiElement psi = parseText("add\n  x := [foo\n   ]$List\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testPlusSlash() {
        PsiElement psi = parseText("new(+/[#s for s in l], space$C)\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }



    public void testIfStatement() {
        PsiElement psi = parseText("if R has X\n then\n  foo\n else\n  bar\nZZZ\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testIfStatement2() {
        PsiElement psi = parseText("if R has X\nthen\n  foo\nelse\n bar\nZZZ\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }


    public void testParseCatDef() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/catdef.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseCDen() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/cden.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseAggCat() throws IOException {
        assertNotNull(getProject());
        // Fails: Line 722      "++ to become an in order iterator" seems misplaced
        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/aggcat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseFR() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/fr.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseFMod() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/fmod.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseSuchThat() throws IOException {
        assertNotNull(getProject());
        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/suchthat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseNumeric() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/numeric.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseAlgFact() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/algfact.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseMultPoly() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/multpoly.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testRadEigen() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/radeigen.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    // FIXME Temp for testing
    public void testR() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/r.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseXHash() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/xhash.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParsePlotTool() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/plottool.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testXLPoly() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/xlpoly.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testJet() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/jet.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testLodof() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/lodof.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseOmCat() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/omcat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseFree() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/free.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseString() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/string.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseFortran() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/fortran.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    private PsiElement parseFile(File file) throws IOException {
        String text = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
        System.out.println(LexerFunctions.tokens(Spad, text).values().stream().map(t -> AldorTokenTypes.isNewLine(t) ? (t + "\n") : (t + " ")).collect(Collectors.joining()));
        return parseText(text);
    }

    // Current fails
    // aggcat: foo ==\n  Join(X)\n add...


    public void testAlgebraLibrary() {
        assertNotNull(getProject());

        File base = new File("/home/pab/Work/fricas/fricas/src/algebra");
        Multimap<FailReason, File> badFiles = parseLibrary(getProject(), base, Sets.newHashSet(
                "texmacs.spad" // Contains markup
        ));

        for (Map.Entry<FailReason, File> ent: badFiles.entries()) {
            System.out.println("Failed: " + ent.getKey() + " --> " + ent.getValue());
        }
        assertTrue(badFiles.isEmpty());
    }


    private PsiElement parseText(CharSequence text) {
        //noinspection StringConcatenationMissingWhitespace
        System.out.println(LexerFunctions.tokens(Spad, text).values().stream().map(t -> (t + (AldorTokenTypes.isNewLine(t) ? "\n" : " "))).collect(Collectors.joining()));
        return parseText(text, AldorTypes.SPAD_TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseSpadText(getProject(), text, elementType);
    }


}
