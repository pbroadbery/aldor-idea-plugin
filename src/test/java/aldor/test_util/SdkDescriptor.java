package aldor.test_util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

interface SdkDescriptor {
    @SuppressWarnings("ClassReferencesSubclass")
    SdkOption sdkOption();

    String name(String prefix);

    Sdk editSdk(Sdk theSdk);

    ModuleType<?> getModuleType();

    void editFacet(Module module);

    JpsModuleSourceRootType<?> rootType();

    SourceFileStorageType sourceFileType();
}
