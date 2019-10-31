package aldor.hierarchy;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collection;

public class AldorFlatHierarchyTreeStructureTest2 {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    //private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));
    private final MyTestRule codeInsightRule = new MyTestRule(getProjectDescriptor(fricasExecutableRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToInfoTestRule)
                    .around(fricasExecutableRule)
                    .around(codeInsightRule.rule)
                    .around(JUnits.prePostTestRule(() -> {}, () -> {}));

    static class MyTestRule extends LightPlatformCodeInsightFixture4TestCase {
        private final LightProjectDescriptor descriptor;

        MyTestRule(LightProjectDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        protected void setUp() throws Exception {
            super.setUp();
            myFixture.setTestDataPath("AldorFlatHierarchyTreeStructureTest2_src");
        }

        @Override
        protected LightProjectDescriptor getProjectDescriptor() {
            return descriptor;
        }

        @Override
        public Project getProject() {
            return super.getProject();
        }
    }

    @Test
    public void testReference() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("Integer", codeInsightRule.getProject(),
                                                                                GlobalSearchScope.allScope(codeInsightRule.getProject()));

        AldorIdentifier identifier = items.iterator().next().defineIdentifier().get();

        Syntax syntax = SyntaxPsiParser.parse(identifier);

        AldorFlatHierarchyTreeStructure treeStructure = new AldorFlatHierarchyTreeStructure(codeInsightRule.getProject(), syntax);

        HierarchyNodeDescriptor base = treeStructure.getBaseDescriptor();
        Object[] children = treeStructure.getChildElements(base);

        System.out.println("Children: " + Arrays.asList(children));

        //assertTrue(false);
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule.prefix());

    }

}