package aldor.module.template;

import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.sdk.aldor.AldorLocalSdkType;
import aldor.sdk.fricas.FricasLocalSdkType;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class AldorGitModuleBuilder extends AldorModuleBuilder {
    private static final String FLD_SOURCEDIR = "aldor";
    private static final String FLD_USEEXISTING = "useExisting";
    private static final String FLD_BUILDDIR = "build";
    private final Map<String, WizardInputField<?>> additionalFieldsByName;
    private final List<WizardInputField<?>> additionalFields;
    private final GitModuleDetail detail;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    AldorGitModuleBuilder(GitModuleType type) {
        super(AldorModuleType.instance());
        this.detail = type.fn().apply(this);
        additionalFields = createAdditionalFields();
        this.additionalFieldsByName = additionalFields.stream().collect(Collectors.toMap(WizardInputField::getId, f -> f));
    }

    private List<WizardInputField<?>> createAdditionalFields() {
        List<WizardInputField<?>> fields = new ArrayList<>();
        fields.add(new WizardCheckBox(FLD_USEEXISTING, "Use existing configuration", true));
        fields.add(new WizardTextField(FLD_SOURCEDIR, "Source directory", "aldor", this::validateSourceDirectory));
        fields.add(new WizardTextField(FLD_BUILDDIR, "Build directory", "build", this::validateBuildDirectory));

        return fields;
    }

    @Nullable
    private String validateBuildDirectory(String path) {
        return validateDirectory(path) ? null : "Invalid build directory";
    }

    @Nullable
    private String validateSourceDirectory(String path) {
        return validateDirectory(path + "/.git") ? null: "Invalid source directory";
    }

    private boolean validateDirectory(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<WizardInputField> getAdditionalFields() {
        return new ArrayList<>(additionalFieldsByName.values());
    }

    @Override
    public String getPresentableName() {
        return detail.name() + " Git Module";
    }

    @Override
    public String getDescription() {
        return detail.name() + " Module cloned from git repository";
    }


    @Override
    public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        super.setupRootModel(modifiableRootModel);
        detail.setupRootModel(modifiableRootModel);
    }


    public class AldorGitModuleDetail implements GitModuleDetail {

        @Override
        public boolean isSuitableSdkType(SdkTypeId sdkType) {
            return (sdkType instanceof AldorLocalSdkType);
        }

        @Override
        public String name() {
            return "Aldor";
        }

        @Override
        public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
            String contentEntryPath = getContentEntryPath();
            String sourceDirectory = additionalFieldsByName.get(FLD_SOURCEDIR).getValue();

            if (StringUtil.isEmpty(contentEntryPath)) {
                return;
            }

            ContentEntry entry = modifiableRootModel.getContentEntries()[0];
            if (entry.getFile() == null) {
                return;
            }

            VirtualFile gitDirectory = entry.getFile().findFileByRelativePath(sourceDirectory + "/.git");
            if (gitDirectory == null) {
                File file = new File(entry.getFile().getPath() + "/" + sourceDirectory);
                throw new ConfigurationException("Missing git repository - expecting repository in " + file);
            }

            String[] paths = { "aldor/lib/aldor", "aldor/lib/algebra" };
            for (String path: paths) {
                VirtualFile file = entry.getFile().findFileByRelativePath(sourceDirectory + "/" + path);
                if (file != null) {
                    entry.addSourceFolder(file, false);
                }
            }
        }

    }

    public class FricasGitModuleDetail implements GitModuleDetail {

        @Override
        public boolean isSuitableSdkType(SdkTypeId sdkType) {
            return (sdkType instanceof FricasLocalSdkType);
        }

        @Override
        public String name() {
            return "Fricas";
        }

        @Override
        public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
            String contentEntryPath = getContentEntryPath();
            String sourceDirectory = additionalFieldsByName.get(FLD_SOURCEDIR).getValue();

            if (StringUtil.isEmpty(contentEntryPath)) {
                return;
            }

            ContentEntry entry = modifiableRootModel.getContentEntries()[0];
            if (entry.getFile() == null) {
                return;
            }

            VirtualFile gitDirectory = entry.getFile().findFileByRelativePath(sourceDirectory + "/.git");
            if (gitDirectory == null) {
                File file = new File(entry.getFile().getPath() + "/" + sourceDirectory);
                throw new ConfigurationException("Missing git repository - expecting repository in " + file);
            }

            String[] paths = { "fricas/src/algebra" };
            for (String path: paths) {
                VirtualFile file = entry.getFile().findFileByRelativePath(sourceDirectory + "/" + path);
                if (file != null) {
                    entry.addSourceFolder(file, false);
                }
            }
        }

    }

}
