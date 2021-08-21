package aldor.psi;

import aldor.file.AldorFileType;
import aldor.file.SpadFileType;
import aldor.psi.impl.AldorTopLevelImpl;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AldorPsiUtilsTest extends AssumptionAware.BasePlatformTestCase {

    public void testIsTopLevel() {
        PsiFile file = createLightFile(AldorFileType.INSTANCE, "A: B == C");

        AldorDefine definition = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(definition);
        Assert.assertTrue(AldorPsiUtils.isTopLevel(definition.getParent()));
    }

    public void testIsTopLevelDefine() {
        PsiFile file = createLightFile(AldorFileType.INSTANCE, "define A: B == C");

        AldorDefine definition = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(definition);
        Assert.assertTrue(AldorPsiUtils.isTopLevel(definition.getParent()));
    }

    public void testIsTopLevelExtend() {
        PsiFile file = createLightFile(AldorFileType.INSTANCE, "extend A: B == C");

        AldorDefine definition = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(definition);
        Assert.assertTrue(AldorPsiUtils.isTopLevel(definition.getParent()));
    }

    public void testIsTopLevelLocal() {
        PsiFile file = createLightFile(AldorFileType.INSTANCE, "local A: B == C");

        AldorDefine definition = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(definition);
        Assert.assertFalse(AldorPsiUtils.isTopLevel(definition.getParent()));
    }
    public void testIsTopLevelWithWhere() {
        String text = "Outer: B == C where { B ==> with { inner == b }}";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
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

    public void testDefiningFormRhsCase() {
        String text = "foo: A == with {}";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        PsiElement withElt = file.findElementAt(text.indexOf("with"));
        Optional<AldorDefine> defn = AldorPsiUtils.definingForm(withElt);
        Assert.assertTrue(defn.isPresent());
    }

    public void testDefiningFormLhsCase() {
        String text = "foo: with {} == add";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        PsiElement withElt = file.findElementAt(text.indexOf("with"));
        Optional<AldorDefine> defn = AldorPsiUtils.definingForm(withElt);
        Assert.assertTrue(defn.isPresent());
    }

    public void testDefiningFormMacroCase() {
        String text = "foo: E == I where { E ==> with; I ==> add}";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        PsiElement withElt = file.findElementAt(text.indexOf("with"));
        Optional<AldorDefine> defn = AldorPsiUtils.definingForm(withElt);
        Assert.assertTrue(defn.isPresent());
        Assert.assertEquals(0, defn.get().getTextOffset());
    }

    public void testDefiningFormMacroSpadCase() {
        String text = "foo: E == I where { E ==> with; I ==> add}";
        PsiFile file = createLightFile(SpadFileType.INSTANCE, text);
        PsiElement withElt = file.findElementAt(text.indexOf("with"));
        Optional<AldorDefine> defn = AldorPsiUtils.definingForm(withElt);
        Assert.assertTrue(defn.isPresent());
        Assert.assertEquals(0, defn.get().getTextOffset());
    }


    public void testDefinitionClass() {
        String text = "foo: Category == with {}";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertEquals(AldorPsiUtils.DefinitionClass.CATEGORY, AldorPsiUtils.definitionClassForDefine(define));
    }

    public void testDefinitionClass1() {
        String text = "foo: with == add";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertEquals(AldorPsiUtils.DefinitionClass.DOMAIN, AldorPsiUtils.definitionClassForDefine(define));
    }

    public void testDefinitionClass2() {
        String text = "foo: AA == add";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertEquals(AldorPsiUtils.DefinitionClass.DOMAIN, AldorPsiUtils.definitionClassForDefine(define));
    }

    public void testDefinitionClass3() {
        String text = "foo: AA == bbb";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertEquals(AldorPsiUtils.DefinitionClass.VALUE, AldorPsiUtils.definitionClassForDefine(define));
    }

    public void testSearchBindings() {
        String text = "add { foo(x: String): () == never }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        List<AldorPsiUtils.Binding> bindings = AldorPsiUtils.childBindings(Objects.requireNonNull(PsiTreeUtil.findChildOfType(file, AldorUnaryAdd.class)));
        Assert.assertEquals(1, bindings.size());
        Assert.assertEquals("foo", bindings.get(0)
                .maybeAs(AldorDefine.class)
                .flatMap(AldorDefine::defineIdentifier)
                .map(PsiElement::getText)
                .orElse(null));
    }

    public void testSearchLocal() {
        String text = "add { local foo(x: String): () == never }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        List<AldorPsiUtils.Binding> bindings = AldorPsiUtils.childBindings(Objects.requireNonNull(PsiTreeUtil.findChildOfType(file, AldorUnaryAdd.class)));
        Assert.assertEquals(1, bindings.size());
        Assert.assertEquals("foo", bindings.get(0)
                .maybeAs(AldorDefine.class)
                .flatMap(AldorDefine::defineIdentifier)
                .map(PsiElement::getText)
                .orElse(null));
    }

    public void testSearchBindingsWith() {
        String text = "with { xyz: String }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        List<AldorPsiUtils.Binding> bindings = AldorPsiUtils.childBindings(Objects.requireNonNull(PsiTreeUtil.findChildOfType(file, AldorUnaryWith.class)));
        Assert.assertEquals(1, bindings.size());
        Assert.assertEquals("xyz", bindings.get(0)
                .maybeAs(PsiNamedElement.class)
                .map(PsiNamedElement::getName)
                .orElse(null));
    }

    public void testTopLevelDefiningForm() {
        String text = "foo: with blah == add";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        PsiElement elt = PsiTreeUtil.findChildOfType(file, AldorWith.class);
        PsiElement define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(elt);
        Assert.assertEquals(define, AldorPsiUtils.topLevelDefininingForm(elt).orElse(null));
    }

    public void testTopLevelDefiningForm_top() {
        String text = "foo: with blah == add";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        PsiElement elt = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        PsiElement define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(elt);
        Assert.assertEquals(define, AldorPsiUtils.topLevelDefininingForm(elt).orElse(null));
    }

    public void testTopLevelDefiningForm_inner() {
        String text = "foo: with blah == add { qq == return }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        PsiElement elt = PsiTreeUtil.findChildOfType(file, AldorReturnStatement.class);
        PsiElement define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(elt);
        Assert.assertEquals(define, AldorPsiUtils.topLevelDefininingForm(elt).orElse(null));
    }

    public void testChildBindingsFromTopLevel() {
        String text = "Foo: with blah == add { testOne(): () == return; testTwo(): () == never }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(define);
        List<AldorPsiUtils.Binding> childBindings = AldorPsiUtils.childBindings(define.rhs());
        Assert.assertEquals(2, childBindings.size());
    }

    public void testChildBindingsWithDomainDef() {
        String text = "Wibble: with == add; Foo: with {x: String -> String} == add { testOne(): () == return; testTwo(): () == never }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorTopLevel topLevel = PsiTreeUtil.findChildOfType(file, AldorTopLevel.class);
        Assert.assertNotNull(topLevel);
        List<AldorPsiUtils.Binding> childBindings = AldorPsiUtils.childBindings(topLevel);
        Assert.assertEquals(2, childBindings.size());
    }

    public void testUniqueIdentifier() {
        String text = "Foo: A == B where { A ==> 1; B ==> 2 }";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(define);
        Optional<AldorId> id = AldorPsiUtils.findUniqueIdentifier(define.rhs());
        Assert.assertTrue(id.isPresent());
        Assert.assertEquals("B", id.get().getText());
    }

    public void testUniqueIdentifierAdd() {
        String text = "Foo: A == X add";
        PsiFile file = createLightFile(AldorFileType.INSTANCE, text);
        AldorDefine define = PsiTreeUtil.findChildOfType(file, AldorDefine.class);
        Assert.assertNotNull(define);
        Optional<AldorId> id = AldorPsiUtils.findUniqueIdentifier(define.rhs());
        Assert.assertFalse(id.isPresent());
    }
}
