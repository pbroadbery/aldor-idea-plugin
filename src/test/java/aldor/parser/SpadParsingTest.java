package aldor.parser;

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

    public void testLongLine() {
        PsiElement psi = parseText("add\n  x := [foo\n   ]$List\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        logPsi(psi);
        assertEquals(0, errors.size());
    }

    public void testIfStatement() {
        PsiElement psi = parseText("if R has X\n then\n  foo\n else\n bar\nZZZ\n");
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


    public void testParseYStream() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/ystream.spad");
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


    public void testParseOmCat() throws IOException {
        assertNotNull(getProject());

        File file = new File("/home/pab/Work/fricas/fricas/src/algebra/omcat.spad");
        PsiElement psi = parseFile(file);
        logPsi(psi);

        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    private PsiElement parseFile(File file) throws IOException {
        String text = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
        return parseText(text);
    }

    public void testAlgebraLibrary() {
        assertNotNull(getProject());

        File base = new File("/home/pab/Work/fricas/fricas/src/algebra");
        Multimap<FailReason, File> badFiles = parseLibrary(getProject(), base, Sets.newHashSet());

        for (Map.Entry<FailReason, File> ent: badFiles.entries()) {
            System.out.println("Failed: " + ent.getKey() + " --> " + ent.getValue());
        }
        assertTrue(badFiles.isEmpty());
    }


    private PsiElement parseText(String text) {
        return parseText(text, AldorTypes.SPAD_TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseSpadText(getProject(), text, elementType);
    }


}
