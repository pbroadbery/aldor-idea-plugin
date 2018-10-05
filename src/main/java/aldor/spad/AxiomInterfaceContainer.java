package aldor.spad;

import aldor.typelib.AxiomInterface;
import com.intellij.openapi.application.ApplicationManager;
import foamj.Clos;

import java.util.LinkedList;
import java.util.List;

public class AxiomInterfaceContainer {
    private final SpadEnvironment environment;
    private final AldorExecutor aldorExecutor;
    private AxiomInterface iface = null;
    private boolean needsReload = true;
    private final List<SpadLibrary> dependants;

    public AxiomInterfaceContainer(SpadEnvironment spadEnvironment) {
        this.environment = spadEnvironment;
        this.aldorExecutor = ApplicationManager.getApplication().getComponent(AldorExecutor.class);
        this.dependants = new LinkedList<>();
    }

    public AxiomInterface value() {
        if (needsReload) {
            iface = load();
            needsReload = false;
        }
        return iface;
    }

    private AxiomInterface load() {
        try {
            Clos fn = aldorExecutor.createLoadFn("axiomshell");
            fn.call();
            return aldorExecutor.compute(environment::create);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addDependant(SpadLibrary container) {
        this.dependants.add(container);
    }


    public void removeDependant(SpadLibrary container) {
        this.dependants.remove(container);
    }

    public void needsReload() {
        needsReload = true;
        for (SpadLibrary c: dependants) {
            c.needsReload();
        }
    }

    public void dispose() {
    }
}
