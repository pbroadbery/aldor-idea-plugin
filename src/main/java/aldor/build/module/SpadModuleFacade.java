package aldor.build.module;

import com.intellij.openapi.module.Module;

import java.util.Optional;

public interface SpadModuleFacade {
    static Optional<? extends SpadModuleFacade> forModule(Module module) {
        return AldorModuleFacade.forModule(module)
                .or(() -> FricasModuleFacade.forModule(module));
    }

    boolean isConfigured();
}
