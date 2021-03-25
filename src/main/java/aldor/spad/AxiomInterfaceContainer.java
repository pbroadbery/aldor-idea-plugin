package aldor.spad;

import aldor.typelib.AxiomInterface;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import foamj.Clos;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AxiomInterfaceContainer implements Disposable {
    private static final Logger LOG = Logger.getInstance(AxiomInterfaceContainer.class);
    private static final AtomicInteger count = new AtomicInteger(0);
    private final int serialId;

    private final SpadEnvironment environment;
    private final AldorExecutor aldorExecutor;
    private AxiomInterface iface = null;
    private boolean needsReload = true;
    private final List<SpadLibrary> dependants;

    public AxiomInterfaceContainer(SpadEnvironment spadEnvironment) {
        this.environment = spadEnvironment;
        this.aldorExecutor = ApplicationManager.getApplication().getComponent(AldorExecutor.class);
        this.dependants = new LinkedList<>();
        this.serialId = count.incrementAndGet();
        LOG.info("AxiomInterfaceContainer: " + serialId + " created for " + spadEnvironment);
    }

    public AxiomInterface value() {
        if (needsReload) {
            iface = load();
            needsReload = false;
        }
        return iface;
    }

    private AxiomInterface load() {
        LOG.info("AxiomInterfaceContainer: " + serialId + " loading");
        try {
            Clos fn = aldorExecutor.createLoadFn("axiomshell");
            fn.call();
            AxiomInterface iface = aldorExecutor.compute(environment::create);
            LOG.info("AxiomInterfaceContainer: " + serialId + " loaded");
            return iface;
        } catch (InterruptedException e) {
            throw new AldorExecutorException("Interrupted while executing", e);
        }
    }

    public void addDependant(SpadLibrary container) {
        LOG.info("AxiomInterfaceContainer: " + serialId + " adding dependant " + dependants.size() + " - " + container);
        this.dependants.add(container);
    }

    public void removeDependant(SpadLibrary container) {
        LOG.info("AxiomInterfaceContainer: " + serialId + " removing dependant " + container);
        this.dependants.remove(container);
    }

    public void needsReload() {
        LOG.info("AxiomInterfaceContainer: " + serialId + " Needs reload ");
        needsReload = true;
        for (SpadLibrary c: dependants) {
            c.needsReload();
        }
    }

    @Override
    public void dispose() {
        LOG.info("AxiomInterfaceContainer: disposing.. remaining dependants: " + dependants.size());
        dependants.clear();
    }
}
