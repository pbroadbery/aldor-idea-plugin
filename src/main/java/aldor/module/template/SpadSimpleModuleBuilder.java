package aldor.module.template;

import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

class SpadSimpleModuleBuilder extends AldorModuleBuilder {
    private static final Logger LOG = Logger.getInstance(SpadSimpleModuleBuilder.class);

    protected SpadSimpleModuleBuilder() {
        super(AldorModuleType.instance());
    }

    @Override
    public String getPresentableName() {
        return "Simple Spad module";
    }

    @Override
    public String getDescription() {
        return "Spad module";
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
    }

    private VirtualFile getContentRoot() {
        if (getContentEntryPath() == null) {
            throw new IllegalStateException("Missing content root");
        }
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        return fileSystem.findFileByIoFile(new File(getContentEntryPath()));
    }
}
