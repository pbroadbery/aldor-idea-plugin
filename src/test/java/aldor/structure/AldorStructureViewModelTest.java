package aldor.structure;

import aldor.test_util.JUnits;
import aldor.util.VirtualFileTests;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class AldorStructureViewModelTest extends CodeInsightFixtureTestCase {
    private static final Logger LOG = Logger.getInstance(AldorStructureViewModelTest.class);

    public void testViewModel() {
        JUnits.setLogToInfo();
        @NotNull VirtualFile contentRoot = ModuleRootManager.getInstance(myModule).getContentRoots()[0];
        VirtualFile file = VirtualFileTests.createFile(contentRoot, "foo.as", "Foo: with == add");

        myFixture.configureFromExistingVirtualFile(file);
        StructureViewModel model = new AldorStructureViewModel(getEditor(), getFile());

        StructureViewTreeElement root = model.getRoot();
        getEditor().getSelectionModel().setSelection(0, 0);
        Object elt = model.getCurrentEditorElement();
        LOG.info("Children are: " + Arrays.asList(root.getChildren()));
        LOG.info("Elt: " + elt);
        Assert.assertEquals(1, root.getChildren().length);
    }


}
