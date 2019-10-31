package aldor.runconfiguration.aldor;

import aldor.file.AldorFileType;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorPsiUtils;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class AldorUnitConfigurationProducer extends RunConfigurationProducer<AldorUnitConfiguration> {

    protected AldorUnitConfigurationProducer() {
        super(new AldorUnitRunConfigurationType());
    }


    @Override
    protected boolean setupConfigurationFromContext(AldorUnitConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        PsiFile file = sourceElement.get().getContainingFile();
        if ((file != null) && Objects.equals(file.getFileType(), AldorFileType.INSTANCE)) {
            VirtualFile vfile = file.getVirtualFile();
            if (vfile != null) {
                Module module = FileIndexFacade.getInstance(file.getProject()).getModuleForFile(vfile);

                if (module == null) {
                    return false;
                }
                configuration.setModule(module);
                ModuleRootManager.getInstance(module).getFileIndex().isInSourceContent(vfile);
                Optional<VirtualFile> root = Arrays.stream(ModuleRootManager.getInstance(module).getContentRoots()).filter(r -> FileUtil.isAncestor(r.getPath(), vfile.getPath(), false)).findFirst();
                if (!root.isPresent()) {
                    return false;
                }
                String path = FileUtil.getRelativePath(new File(root.get().getPath()), new File(vfile.getPath()));
                configuration.inputFile(path);

                PsiElement elt = sourceElement.get();
                Optional<AldorDefine> define = AldorPsiUtils.topLevelDefininingForm(elt);
                Optional<AldorIdentifier> id = define.flatMap(AldorDefine::defineIdentifier);
                if (id.isPresent()) {
                    configuration.setTypeName(id.get().getText());
                    configuration.setSuggestedName();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(AldorUnitConfiguration configuration, ConfigurationContext context) {
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

}
