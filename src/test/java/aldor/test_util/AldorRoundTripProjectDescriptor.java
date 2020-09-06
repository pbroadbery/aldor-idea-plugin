package aldor.test_util;

import aldor.build.module.AldorModuleType;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

// Use Sdk Project Descriptors
@Deprecated
public class AldorRoundTripProjectDescriptor extends LightProjectDescriptor {
    private Sdk sdk = null;

    @NotNull
    @Override
    public String getModuleTypeId() {
        return AldorModuleType.ID;
    }

    @Override
    public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
        super.setUpProject(project, handler);
        ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
        project.save();
    }

    @Override
    protected void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);
        CompilerModuleExtension compilerModuleExtension = model.getModuleExtension(CompilerModuleExtension.class);
        compilerModuleExtension.setCompilerOutputPath("file:///tmp/test_output");
        compilerModuleExtension.inheritCompilerOutputPath(false);
        System.out.println("Configure " + module);
    }

    // Not needed, except that the compile driver insists on it.
    @Override
    public Sdk getSdk() {
        if (sdk == null) {
            sdk = createSDK();
            ProjectJdkTable.getInstance().addJdk(sdk);
        }
        return sdk;
    }

    private Sdk createSDK() {
        return JavaSdk.getInstance().createJdk("java", "/home/pab/Work/intellij/jdk1.8.0_101");
    }

    @NotNull
    @Override
    public Module createMainModule(@NotNull Project project) {
        Module m = super.createMainModule(project);
        return m;
    }

    @Override
    protected VirtualFile createSourceRoot(@NotNull Module module, String srcPath) {
        try {
            VirtualFile root = module.getProject().getBaseDir().getFileSystem().findFileByPath("/tmp");
            assert root != null;
            String moduleName = module.getProject().getName() + "_" + module.getName();
            VirtualFile srcRoot = root.findChild(moduleName);
            if (srcRoot == null) {
                    return root.createChildDirectory(null, moduleName);
            }
            else {
                srcRoot.refresh(false, false);
                return srcRoot;
            }
        }catch (IOException e) {
            throw new RuntimeException("No way", e);
        }
    }
}
