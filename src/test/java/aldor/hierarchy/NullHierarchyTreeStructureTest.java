package aldor.hierarchy;

import aldor.hierarchy.util.NullHierarchyTreeStructure;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NullHierarchyTreeStructureTest {
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(new LightProjectDescriptor());

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToDebugTestRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());


    @Test
    public void testNullHierarchy() {
        String text = "List";
        PsiFile whole = codeTestFixture.addFileToProject("test.spad", text);

        NullHierarchyTreeStructure structure = new NullHierarchyTreeStructure(whole, "hamsters");

        HierarchyNodeDescriptor base = structure.getBaseDescriptor();
        base.update();
        assertTrue(base.isValid());
        System.out.println("Base: " + base);
        Object[] children = structure.getChildElements(base);
        assertEquals(1, children.length);
        NodeDescriptor<?> child = (NodeDescriptor<?>) children[0];
        child.update();
        System.out.println("Child: " + child);
        assertEquals(0, structure.getChildElements(child).length);
    }
}
