package aldor.hierarchy;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorFlatHierarchyTreeStructureTest2 {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));
    //private final MyTestRule codeInsightRule = new MyTestRule(getProjectDescriptor(fricasExecutableRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToDebugTestRule)
                    .around(fricasExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.prePostTestRule(() -> codeTestFixture.getProject().save(), () -> {}))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testReference() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("Integer", codeTestFixture.getProject(),
                                                                                GlobalSearchScope.allScope(codeTestFixture.getProject()));


        Optional<AldorIdentifier> aldorIdentifier = items.iterator().next().defineIdentifier();
        assertTrue(aldorIdentifier.isPresent());
        AldorIdentifier identifier = aldorIdentifier.get();

        Syntax syntax = SyntaxPsiParser.parse(identifier);
        assertNotNull(syntax);
        AldorFlatHierarchyTreeStructure treeStructure = new AldorFlatHierarchyTreeStructure(codeTestFixture.getProject(), syntax);

        HierarchyNodeDescriptor base = treeStructure.getBaseDescriptor();
        Object[] children = treeStructure.getChildElements(base);

        System.out.println("Children: " + Arrays.asList(children));

        //assertTrue(false);
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule);

    }

}