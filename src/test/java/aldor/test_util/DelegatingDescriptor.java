package aldor.test_util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

class DelegatingDescriptor implements SdkDescriptor {
    private final SdkDescriptor innerSdkDescriptor;

    DelegatingDescriptor(SdkDescriptor descriptor) {
        this.innerSdkDescriptor = descriptor;
    }

    @Override
    public SdkOption sdkOption() {
        return innerSdkDescriptor.sdkOption();
    }

    @Override
    public String name(String prefix) {
        return innerSdkDescriptor.name(prefix) + "_AldorUnit";
    }

    @Override
    public Sdk editSdk(Sdk theSdk) {
        return innerSdkDescriptor.editSdk(theSdk);
    }

    @Override
    public ModuleType<?> getModuleType() {
        return innerSdkDescriptor.getModuleType();
    }

    @Override
    public void editFacet(Module module) {
        innerSdkDescriptor.editFacet(module);
    }

    @Override
    public JpsModuleSourceRootType<?> rootType() {
        return innerSdkDescriptor.rootType();
    }

    @Override
    public SourceFileStorageType sourceFileType() {
        return innerSdkDescriptor.sourceFileType();
    }
}
