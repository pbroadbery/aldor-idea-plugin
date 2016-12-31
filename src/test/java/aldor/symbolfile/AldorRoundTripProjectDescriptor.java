package aldor.symbolfile;

import aldor.build.module.AldorModuleType;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class AldorRoundTripProjectDescriptor extends LightProjectDescriptor {
    @Override
    public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
        super.setUpProject(project, handler);
        ApplicationManagerEx.getApplicationEx().doNotSave(false);
        project.save();
    }

    @Override
    protected void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);
        CompilerModuleExtension compilerModuleExtension = model.getModuleExtension(CompilerModuleExtension.class);
        compilerModuleExtension.setCompilerOutputPath("file:///tmp");
        compilerModuleExtension.inheritCompilerOutputPath(false);
    }

    // Not needed, except that the compile driver insists on it.
    @Override
    public Sdk getSdk() {
        JavaSdk x = JavaSdk.getInstance();
        return JavaSdk.getInstance().createJdk("java", "/home/pab/Work/intellij/jdk1.8.0_101");
    }

    @Override
    @NotNull
    public ModuleType<?> getModuleType() {
        return AldorModuleType.instance();
    }

    @Override
    protected VirtualFile createSourceRoot(@NotNull Module module, String srcPath) {
        return module.getProject().getBaseDir();
    }
}
