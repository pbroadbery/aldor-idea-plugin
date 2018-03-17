package aldor.editor;

import aldor.file.AldorFileType;
import aldor.parser.ParserFunctions;
import aldor.psi.AldorDefine;
import aldor.test_util.LightProjectDescriptors;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

public class AldorLookupElementFactoryTest extends LightPlatformCodeInsightFixtureTestCase {
    private final PsiElementToLookupElementMapping factory = new AldorLookupElementFactory();

    public void testDefine() {
        String text = "foo(x: A): B == 2";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        LookupElement elt = factory.forConstant(define);
        Assert.assertNotNull(elt);
    }

    public void testConst() {
        String text = "foo == 2";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        LookupElement elt = factory.forConstant(define);
        Assert.assertNotNull(elt);
    }

    public void testTypedConst() {
        String text = "foo: X == 2";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        LookupElement elt = factory.forMacro(define);
        Assert.assertNotNull(elt);
    }

    public void testMacro() {
        String text = "foo ==> 2";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        LookupElement elt = factory.forMacro(define);
        Assert.assertNotNull(elt);
    }

    public void testMacroParms() {
        String text = "FOO(X) ==> 2";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        LookupElement elt = factory.forConstant(define);
        Assert.assertNotNull(elt);
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text);
    }


    private PsiElement parseText(CharSequence text, IElementType type) {
        return ParserFunctions.parseAldorText(getProject(), text, type);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptors.ALDOR_MODULE_DESCRIPTOR;
    }

}