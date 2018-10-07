package aldor.parser;

import aldor.lexer.AldorTokenTypes;
import aldor.lexer.LexerFunctions;
import aldor.psi.elements.AldorTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.junit.Assert;

import java.util.List;
import java.util.stream.Collectors;

import static aldor.lexer.LexMode.Spad;
import static aldor.psi.AldorPsiUtils.logPsi;

public class SysCmdParsingTest extends LightPlatformCodeInsightTestCase {

    public void testAbbrev() {
        //PsiFile file = createFile("foo.spad", ")abbrev Foo Foo Foo Foo\nFoo==2");
        PsiFile file = createFile("foo.spad", ")abbrev Foo Foo Foo\nFoo==2");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(file);
        logPsi(file);
        Assert.assertEquals(0, errors.size());
    }
    private PsiElement parseText(CharSequence text) {
        System.out.println(LexerFunctions.tokens(Spad, text).values().stream().map(t -> (t + (AldorTokenTypes.isNewLine(t) ? "\n" : " "))).collect(Collectors.joining()));
        return parseText(text, AldorTypes.SPAD_TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseSpadText(getProject(), text, elementType);
    }


}
