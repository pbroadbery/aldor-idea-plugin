package aldor.runconfiguration.spad;

import aldor.file.SpadInputFileType;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpadInputRunConfigurationProducer extends LazyRunConfigurationProducer<SpadInputConfiguration> {

    protected SpadInputRunConfigurationProducer() {
    }


    @Override
    protected boolean setupConfigurationFromContext(@NotNull SpadInputConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        PsiFile file = sourceElement.get().getContainingFile();
        if ((file != null) && Objects.equals(file.getFileType(), SpadInputFileType.INSTANCE)) {
            VirtualFile vfile = file.getVirtualFile();
            if (vfile != null) {
                Module module = FileIndexFacade.getInstance(file.getProject()).getModuleForFile(vfile);

                configuration.inputFile(vfile.getPath());
                configuration.setName(file.getName());

                if (module != null) {
                    configuration.setModule(module);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull SpadInputConfiguration configuration, ConfigurationContext context) {
        final PsiElement location = context.getPsiLocation();
        if (location == null) {
            return false;
        }
        PsiFile file = location.getContainingFile();
        if ((file == null) || (file.getVirtualFile() == null)) {
            return false;
        }
        return FileUtil.pathsEqual(file.getVirtualFile().getPath(), configuration.inputFile());
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return SpadInputRunConfigurationType.instance();
    }
}
