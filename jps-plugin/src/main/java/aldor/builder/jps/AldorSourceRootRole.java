package aldor.builder.jps;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsEventDispatcher;

public class AldorSourceRootRole extends JpsElementChildRole<AldorSourceRootProperties> {
    private static final Logger LOG = Logger.getInstance(AldorSourceRootRole.class);
    public static final AldorSourceRootRole INSTANCE = new AldorSourceRootRole();

    @Override
    public void fireElementAdded(@NotNull JpsEventDispatcher dispatcher, @NotNull AldorSourceRootProperties element) {
        LOG.info("AldorSourceRootRole::Added " + dispatcher + " " + element);
    }

    @Override
    public void fireElementRemoved(@NotNull JpsEventDispatcher dispatcher, @NotNull AldorSourceRootProperties element) {
        LOG.info("AldorSourceRootRole::Removed " + dispatcher + " " + element);
    }
}
