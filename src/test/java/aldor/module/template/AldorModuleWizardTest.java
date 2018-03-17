package aldor.module.template;

import aldor.test_util.SkipCI;
import com.intellij.ide.util.projectWizard.AbstractModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.CompositeDisposable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.testFramework.PlatformTestCase;
import org.junit.Assert;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("JUnitTestCaseWithNoTests")
@Ignore("Need to revisit & turn into junit4") public class AldorModuleWizardTest extends PlatformTestCase {

    @SkipCI
    public void testCreateProject() {
        Disposable disposable = new CompositeDisposable() {};
        WizardContext context = new WizardContext(getProject(), disposable);
        ProjectTemplate[] templates = new AldorGitTemplateFactory().createTemplates("Aldor", context);

        ProjectTemplate template = Arrays.stream(templates).filter(t -> "Simple Aldor module".equals(t.getName())).findFirst().orElse(null);
        Assert.assertNotNull(template);

        AbstractModuleBuilder builder = template.createModuleBuilder();
        builder.setName("SillyModule");

        List<Module> modules = builder.commit(getProject());
        Assert.assertEquals(1, modules.size());
        Module module = modules.get(0);
        Assert.assertEquals("SillyModule", module.getName());
        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        VirtualFile mf = roots[0].findChild("Makefile");
        Assert.assertNotNull(mf);
        Disposer.dispose(disposable);
    }

}
