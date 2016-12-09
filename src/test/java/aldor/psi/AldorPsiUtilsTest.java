package aldor.psi;

import aldor.build.module.AldorModuleType;
import aldor.file.AldorFileType;
import aldor.psi.AldorDefineStubbing.AldorDefine;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import static aldor.psi.AldorPsiUtils.logPsi;

public class AldorPsiUtilsTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testIsTopLevel() throws Exception {
        PsiFile file = createLightFile(AldorFileType.INSTANCE, "A: B == C");

        AldorDefine definition = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(definition);
        Assert.assertTrue(AldorPsiUtils.isTopLevel(definition.getParent()));
    }

    public void testIsTopLevelWithWhere() throws Exception {
        String text = "Outer: B == C where { B ==> with { inner == b }}";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
logPsi(file);
        AldorDefine definition = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(definition);
        Assert.assertTrue(AldorPsiUtils.isTopLevel(definition.getParent()));

        PsiElement elt = file.findElementAt(text.indexOf("inner"));
        while (!(elt instanceof AldorDefine)) {
            Assert.assertNotNull(elt);
            elt = elt.getParent();
        }
        Assert.assertFalse(AldorPsiUtils.isTopLevel(elt.getParent()));
        PsiElement elt2 = file.findElementAt(text.indexOf("inner"));
        while (!(elt2 instanceof AldorDefine)) {
            Assert.assertNotNull(elt2);
            elt2 = elt2.getParent();
        }
        Assert.assertFalse(AldorPsiUtils.isTopLevel(elt2.getParent()));
    }


    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        //noinspection ReturnOfInnerClass
        return new LightProjectDescriptor() {

            @Override
            @NotNull
            public ModuleType<?> getModuleType() {
                return AldorModuleType.instance();
            }


        };
    }
}
