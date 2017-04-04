package aldor.module.template;

import com.intellij.testFramework.PlatformTestCase;
import org.junit.Ignore;

@SuppressWarnings("JUnitTestCaseWithNoTests")
@Ignore("Need to revisit & turn into junit4")
public class AldorModuleWizardTest extends PlatformTestCase {

    /*
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
        disposable.dispose();
    }
    */
}
