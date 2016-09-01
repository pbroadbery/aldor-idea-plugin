package pab.aldor;

import aldor.AldorParserDefinition;
import aldor.AldorTypes;
import com.google.common.collect.Lists;
import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer Test. Created by pab on 30/08/16.
 */
public class EnsureParsingTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new AldorProjectDescriptor();
    }

    public void testParseCatDefinition() {
        String text = "X: with == add";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseCommentDefinitions() {
        String text = "X: with == add; Y: with == add";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseMap() {
        String text = "((A: Tuple Type) -> (R: Tuple Type)): with == add;";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseWithBlock() {
        String text = "QQ: with { f: % } == add";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseAddBlock() {
        String text = "QQ: with { f: % } == add { f: % == 23 }";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseError() {
        String text = ":= 2";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(1, errors.size());
    }


    public void testParseAddBlock2() {
        String text = "QQ: with { f: % } == XX add { f: % == 23 }";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }


    public void testParseDefn() {
        String text = "f(x: Z): Z == never";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }


    public void testParseFnDefn() {
        String text = "f(x: Z): Z == 1";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testNegateFnDefn() {
        String text = "-(x: Z): Z == 1";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testFn() {
        String text = "f x";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testNegate() {
        String text = "-1";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        System.out.println("Errors: " + errors);
        assertEquals(0, errors.size());
    }

    public void testParseNothing() {
        String text = "with { f: () -> () }";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseApplication() {
        String text = "f x";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseApplication2() {
        String text = "f g x";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseSequence() {
        String text = "with { f: X; g: Y }";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testParseDomain() {
        String text = "A: with { f: X; g: BInt -> () } == add { dispose!(x: %): () == never }";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testDeclInfix() {
        String text = "+: %";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testDefSeq() {
        String text = "a == add {} b == add {}";
        PsiElement psi = parseText(text);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testEmpty() {
        ParserDefinition aldorParserDefinition = new AldorParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(aldorParserDefinition, aldorParserDefinition.createLexer(null),
                "");

        PsiParser parser = aldorParserDefinition.createParser(getProject());
        ASTNode parsed = parser.parse(AldorTypes.OPT_APPLICATION, psiBuilder);

        logPsi(parsed.getPsi(), 0);
        assertTrue(0 == getPsiErrorElements(parsed.getPsi()).size());
    }

    private PsiElement parseText(String text) {
        ParserDefinition aldorParserDefinition = new AldorParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(aldorParserDefinition, aldorParserDefinition.createLexer(null),
                text);

        PsiParser parser = aldorParserDefinition.createParser(getProject());
        ASTNode parsed = parser.parse(AldorTypes.CURLY_CONTENTS_LABELLED, psiBuilder);

        return parsed.getPsi();
    }

    public void testParseLang() {
        assertNotNull(getProject());

        Project project = getProject();
        File file = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src/lang/sal_lang.as");
        assertTrue(file.exists());
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
        assertNotNull(vf);
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
        //noinspection ConstantConditions
        String text = psiFile.getText();

        PsiElement psi = parseText(text);

        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testAldorLibrary() {
        assertNotNull(getProject());

        Project project = getProject();
        File base = new File("/home/pab/Work/aldorgit/aldor/aldor/lib/aldor/src");
        List<File> files = findAllSource(base);
        for (File file: files) {
            System.out.println("Reading: " + file);
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
            assertNotNull(vf);
            PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
            assertNotNull(psiFile);
            String text = psiFile.getText();

            PsiElement psi = parseText(text);

            final List<PsiErrorElement> errors = getPsiErrorElements(psi);
            assertEquals(0, errors.size());
        }
    }

    private List<File> findAllSource(File base) {
        List<File> files = Lists.newArrayList();
        //noinspection ConstantConditions // list files => NPE?
        for (File file: base.listFiles()) {
            if (file.isDirectory())
                files.addAll(findAllSource(file));
            if (file.getName().endsWith(".as"))
                files.add(file);
        }
        return files;
    }

    private void logPsi(PsiElement psi, int i) {
        String text = (psi.getChildren().length == 0) ? psi.getText(): "";
        System.out.println("(psi: " + psi + " " + text);
        for (PsiElement elt: psi.getChildren()) {
            logPsi(elt, i+1);
        }
        System.out.println(")");
    }

    @NotNull
    private List<PsiErrorElement> getPsiErrorElements(PsiElement psi) {
        final List<PsiErrorElement> errors = new ArrayList<>();

        psi.accept(new PsiRecursiveElementVisitor() {

            @Override
            public void visitErrorElement(PsiErrorElement element) {
                errors.add(element);
                super.visitErrorElement(element);
            }
        });
        return errors;
    }


    private static class AldorProjectDescriptor extends LightProjectDescriptor {

    }


}
