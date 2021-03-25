package aldor.spad;

import com.intellij.openapi.Disposable;

public interface AldorModuleSpadLibraryManager extends Disposable {
    // This is a bit horrible - this class should be responsible for creating SpadLibraries for specific modules
    void register(FricasSpadLibrary library);
}
