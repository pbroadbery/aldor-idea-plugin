package aldor.module.template;

import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

import static aldor.module.template.TemplateFiles.saveFile;

class AldorSimpleModuleBuilder extends AldorModuleBuilder {
    private static final Logger LOG = Logger.getInstance(AldorSimpleModuleBuilder.class);

    protected AldorSimpleModuleBuilder() {
        super(AldorModuleType.instance());
    }

    @Override
    public String getPresentableName() {
        return "Simple Aldor module";
    }

    @Override
    public String getDescription() {
        return "Aldor module with Makefile created";
    }

    @Override
    public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        super.setupRootModel(modifiableRootModel);
        String contentEntryPath = getContentEntryPath();
        if (StringUtil.isEmpty(contentEntryPath)) {
            return;
        }

        File contentRootDir = new File(contentEntryPath);
        createFileLayout(contentRootDir, modifiableRootModel);

        ContentEntry entry = modifiableRootModel.getContentEntries()[0];
        if (entry.getFile() != null) {
            VirtualFile file = entry.getFile().findFileByRelativePath(contentRootDir.getAbsolutePath());

            if (file != null) {
                entry.addSourceFolder(file, false);
            }
        }

    }

    private void createFileLayout(File contentRootDir, ModifiableRootModel model) throws ConfigurationException {
        VirtualFile file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "Makefile");
        if (file == null) {
            return;
        }
        Map<String, String> map = Maps.newHashMap();
        map.put("PROJECT", model.getProject().getName());
        map.put("MODULE", model.getModule().getName());
        map.put("ALDOR_SDK", (model.getSdk() == null) ? "\"SDK path goes here\"" : model.getSdk().getHomePath());
        map.put("INITIAL_ALDOR_FILES", "example");
        saveFile(model.getProject(), file, "Makefile.none", map);

        file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "example.as");
        if (file == null) {
            return;
        }
        saveFile(model.getProject(), file, "example.as", map);

    }

    @Nullable
    private static VirtualFile getOrCreateExternalProjectConfigFile(@NotNull String parent, @NotNull String fileName) {
        File file = new File(parent, fileName);
        FileUtilRt.createIfNotExists(file);
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }


    private VirtualFile getContentRoot() {
        if (getContentEntryPath() == null) {
            throw new IllegalStateException("Missing content root");
        }
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        return fileSystem.findFileByIoFile(new File(getContentEntryPath()));
    }
}
