package aldor.parser;

import aldor.lexer.LexMode;
import aldor.lexer.LexerFunctions;
import aldor.psi.AldorPsiUtils;
import aldor.psi.elements.AldorTypes;
import aldor.test_util.AssumptionAware;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.List;

public class TopLevelParseTest extends AssumptionAware.LightPlatformCodeInsightTestCase {

    public void testTopLevelStd() {
        PsiElement psi = parseText("Foo: with == add \nQQQ: Category == with\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    public void testTopLevelSpadRecovery() {
        String text = ")abbrev category FOO Foo\n" +
                "Foo: with == add\n" +
                "  wibble() == import\n" +
                ")abbrev category BAR Bar\n" +
                "Bar: Category == with\n";

        System.out.println("Tokens " + LexerFunctions.tokens(LexMode.Spad, text));
        ParserDefinition spadParserDefinition = new SpadParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(spadParserDefinition, spadParserDefinition.createLexer(getProject()),
                text);

        PsiParser parser = spadParserDefinition.createParser(getProject());

        @NotNull ASTNode elt = parser.parse(AldorTypes.TOP_LEVEL, psiBuilder);
        AldorPsiUtils.logPsi(elt.getPsi());
    }

    public void testTopLevelSpadRecovery2() {
        String text = ")abbrev category FOO Foo\n" +
                "Foo: with == add\n" +
                "  wibble() == +\n" +
                ")abbrev category BAR Bar\n" +
                "Bar: Category == with\n";

        System.out.println("Tokens " + LexerFunctions.tokens(LexMode.Spad, text));
        ParserDefinition spadParserDefinition = new SpadParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(spadParserDefinition, spadParserDefinition.createLexer(getProject()),
                text);

        PsiParser parser = spadParserDefinition.createParser(getProject());

        @NotNull ASTNode elt = parser.parse(AldorTypes.SPAD_TOP_LEVEL, psiBuilder);

        AldorPsiUtils.logPsi(elt.getPsi());
        //@NotNull Collection<AldorDefineMixin> kids = PsiTreeUtil.collectElementsOfType(elt.getPsi(), AldorDefineMixin.class);
        //kids.forEach(k -> System.out.println(k.getText()));
    }


    private PsiElement parseText(CharSequence text) {
        return parseText(text, AldorTypes.TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseAldorText(getProject(), text, elementType);
    }

    private PsiElement parseSpadText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseSpadText(getProject(), text, elementType);
    }

}
