package aldor.test_util;

import aldor.build.module.AldorModuleType;
import aldor.builder.jps.AldorSourceRootType;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.sdk.aldor.AldorLocalSdkType;
import aldor.sdk.fricas.FricasInstalledSdkType;
import aldor.sdk.fricas.FricasLocalSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

public enum SdkOption implements SdkDescriptor {

    Fricas(new FricasInstalledSdkType(), JavaSourceRootType.SOURCE),
    Aldor(new AldorInstalledSdkType(), AldorSourceRootType.INSTANCE),
    FricasLocal(new FricasLocalSdkType(), JavaSourceRootType.SOURCE),
    AldorLocal(new AldorLocalSdkType(), AldorSourceRootType.INSTANCE);

    private final SdkType sdkType;
    private final JpsModuleSourceRootType<?> sourceRootType;

    SdkOption(SdkType type, JpsModuleSourceRootType<?> rootType) {
        this.sdkType = type;
        this.sourceRootType = rootType;
    }

    @Nullable
    public SdkType sdkType() {
        return sdkType;
    }

    @Override
    public SdkOption sdkOption() {
        return this;
    }

    @Override
    public String name(String prefix) {
        return name() + "_" + prefix;
    }

    @Override
    public Sdk editSdk(Sdk theSdk) {
        return theSdk;
    }

    @Override
    public ModuleType getModuleType() {
        return AldorModuleType.instance();
    }

    @Override
    public void editFacet(Module module) {
    }

    @Override
    public SourceFileStorageType sourceFileType() {
        return SourceFileStorageType.Virtual;
    }

    @Override
    public JpsModuleSourceRootType<?> rootType() {
        return sourceRootType;
    }

    SdkDescriptor withStorageType(SourceFileStorageType storageType) {
        return new WithSourceStorageType(this, storageType);
    }
}
