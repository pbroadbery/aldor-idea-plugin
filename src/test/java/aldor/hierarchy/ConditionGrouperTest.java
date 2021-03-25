package aldor.hierarchy;

import aldor.file.SpadFileType;
import aldor.parser.ParserFunctions;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxTest;
import aldor.test_util.EnsureClosedRule;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.List;
import java.util.concurrent.locks.Condition;

import static aldor.syntax.SyntaxPsiParser.parse;
import static org.junit.Assert.*;

public class ConditionGrouperTest {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));
    private final EnsureClosedRule ensureClosedRule = new EnsureClosedRule();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToDebugTestRule)
                    .around(fricasExecutableRule)
                    .around(ensureClosedRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void test() {
        SpadLibrary library = SpadLibraryManager
                .getInstance(codeTestFixture.getProject())
                .forModule(codeTestFixture.getModule(), SpadFileType.INSTANCE);
        ConditionGrouper grouper = new ConditionGrouper(library);
        PsiElement psi = ParserFunctions.parseAldorText(codeTestFixture.getProject(), "LeftModule Wibble");
        Syntax syntax = parse(psi);

        List<ConditionGrouper.SyntaxWithCondition> parents = grouper.parents(syntax);

        parents.stream().map(p -> p.syntax() + " " + p.conditions()).sorted().forEach(x -> System.out.println("P: " + x));
        for (ConditionGrouper.SyntaxWithCondition parent: parents) {
            System.out.println("Parent: " + parent.syntax() + " " + parent.conditions());
        }
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule.prefix());

    }

}