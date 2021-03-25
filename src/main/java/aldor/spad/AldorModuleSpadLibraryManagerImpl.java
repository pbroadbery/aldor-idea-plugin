package aldor.spad;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;

public final class AldorModuleSpadLibraryManagerImpl implements AldorModuleSpadLibraryManager, Disposable {
    private static final Logger LOG = Logger.getInstance(AldorModuleSpadLibraryManagerImpl.class);

    private final Module module;

    public AldorModuleSpadLibraryManagerImpl(Module module) {
        this.module = module;
    }
    // This is a bit horrible - this class should be responsible for creating SpadLibraries for specific modules
    @Override
    public void register(FricasSpadLibrary library) {
        Disposer.register(this, library);
    }

    @Override
    public void dispose() {
        LOG.info("Disposing stuff for module.");
    }
}
