package aldor.module.template;

import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.test_util.AssumptionAware;
import aldor.test_util.SkipCI;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.CompositeDisposable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.testFramework.HeavyPlatformTestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AldorModuleWizardTest extends AssumptionAware.HeavyPlatformTestCase {

    @SkipCI
    public void testCreateProject() throws IOException {
        Sdk sdk = new ProjectJdkImpl("Aldor Test SDK", AldorInstalledSdkType.instance());
        SdkModificator modificator = sdk.getSdkModificator();
        modificator.setHomePath("/home/pab/Work/aldorgit/utypes/opt");
        modificator.commitChanges();

        Disposable disposable = new CompositeDisposable() {};
        WizardContext context = new WizardContext(getProject(), disposable);
        ProjectTemplate[] templates = new AldorTemplateFactory().createTemplates("Aldor", context);

        ProjectTemplate template = Arrays.stream(templates).filter(t -> "Simple Aldor module".equals(t.getName())).findFirst().orElse(null);
        Assert.assertNotNull(template);

        AldorSimpleModuleBuilder builder = (AldorSimpleModuleBuilder) template.createModuleBuilder();
        builder.setName("SillyModule");
        builder.setModuleJdk(sdk);

        List<Module> modules = builder.commit(getProject());
        Assert.assertEquals(1, modules.size());
        Module module = modules.get(0);
        Assert.assertEquals("SillyModule", module.getName());
        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        VirtualFile mf = roots[0].findFileByRelativePath("src/Makefile");
        Assert.assertNotNull(mf);
        Runtime.getRuntime().exec("mkdir -p " + roots[0].getPath() + " /tmp/testmodule");
        Runtime.getRuntime().exec("cp -rp " + roots[0].getPath() + " /tmp/testmodule");
        Disposer.dispose(disposable);
    }

}
