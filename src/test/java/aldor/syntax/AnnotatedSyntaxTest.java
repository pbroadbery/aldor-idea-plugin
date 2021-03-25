package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.Timer;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldor.typelib.SymbolDatabase;
import aldor.typelib.TForm;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import foamj.Clos;
import foamj.FoamContext;
import foamj.FoamHelper;
import org.junit.Assert;
import org.junit.Assume;

import java.util.List;

import static aldor.syntax.AnnotatedSyntax.fromSyntax;
import static aldor.syntax.AnnotatedSyntax.toSyntax;
import static aldor.syntax.SyntaxPsiParser.parse;

public class AnnotatedSyntaxTest extends BasePlatformTestCase {
    private final DirectoryPresentRule rule = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-linux-gnu/algebra");
    private AxiomInterface iface = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assume.assumeTrue(rule.isPresent());
        FoamContext ctxt = new FoamContext();
        Clos fn = ctxt.createLoadFn("axiomshell");
        fn.call();
        FoamHelper.setContext(ctxt);
        iface = AxiomInterface.create(SymbolDatabase.daases(rule.path()));

    }

    public void testAnnotatedSyntaxSpeed() throws Exception {
        PsiElement psi = parseText("List String");
        Env env = iface.env();
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);

        AnnotatedAbSyn absyn = fromSyntax(env, syntax);

        Timer timer = new Timer("infer");
        int iterations = 1000;
        try (Timer.TimerRun run = timer.run()) {
            for (int i = 0; i < iterations; i++) {
                TForm tf = iface.infer(absyn);
                List<TForm> parents = iface.directParents(tf);
                Assert.assertFalse(parents.isEmpty());
            }
        }
        double timePerIteration = ((double) timer.duration())/ iterations;
        System.out.println("infer: " + timePerIteration);
        //noinspection MagicNumber
        Assert.assertTrue(timePerIteration < 20.0);
    }


    public void testToFromConversions() {
        PsiElement psi = parseText("List String");
        Env env = iface.env();
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);

        AnnotatedAbSyn absyn = fromSyntax(env, syntax);
        System.out.println("AbSyn is: " +absyn);
        Syntax backAgain = toSyntax(getProject(), GlobalSearchScope.projectScope(getProject()), absyn);

        Assert.assertEquals(syntax.toString(), backAgain.toString());
    }
    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text);
    }

    private PsiElement parseSpadText(CharSequence text) {
        return ParserFunctions.parseSpadText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }

}
