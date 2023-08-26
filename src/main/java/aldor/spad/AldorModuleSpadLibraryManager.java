package aldor.spad;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;

public interface AldorModuleSpadLibraryManager extends Disposable {
    // This is a bit horrible - this class should be responsible for creating SpadLibraries for specific modules
    void register(SpadLibrary library);

    static AldorModuleSpadLibraryManager getInstance(Module module) {
        return module.getService(AldorModuleSpadLibraryManager.class);
    }


}
