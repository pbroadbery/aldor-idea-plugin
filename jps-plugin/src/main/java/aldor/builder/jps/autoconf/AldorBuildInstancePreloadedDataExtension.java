package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.BuildStaticModel;
import aldor.util.HasSxForm;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.PreloadedDataExtension;
import org.jetbrains.jps.cmdline.PreloadedData;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.service.JpsServiceManager;

public class AldorBuildInstancePreloadedDataExtension implements PreloadedDataExtension, HasSxForm {
    private static final Logger LOG = Logger.getInstance(AldorBuildInstancePreloadedDataExtension.class);
    private static final Key<BuildStaticModel> staticModelKey = Key.create("StaticModel");
    private static final Key<JpsModel> jpsModelKey = Key.create("StaticModel");

    public AldorBuildInstancePreloadedDataExtension() {
        LOG.info("Creating AldorBuildInstancePreloadedDataExtension");
    }

    @Nullable
    public static AldorBuildInstancePreloadedDataExtension getInstance() {
        for (PreloadedDataExtension extension : JpsServiceManager.getInstance().getExtensions(PreloadedDataExtension.class)) {
            if (extension instanceof AldorBuildInstancePreloadedDataExtension) {
                return (AldorBuildInstancePreloadedDataExtension)extension;
            }
        }
        return null;
    }

    @SuppressWarnings("TestOnlyProblems")
    @Override
    public void preloadData(PreloadedData data) {
        LOG.info("Preload runner: " + data.getRunner() + " descriptor: " + data.getProjectDescriptor() + " " + (data.getUserDataString()));
        if (data.getUserData(staticModelKey) == null) {
            data.addMessage(new CompilerMessage("Aldor", BuildMessage.Kind.INFO, "Preloading - base data"));
        }
    }

    @SuppressWarnings("TestOnlyProblems")
    @Override
    public void buildSessionInitialized(PreloadedData data) {
        LOG.info("Preload buildInit: " + data.getRunner() + " descriptor: " + data.getProjectDescriptor() + " " + (data.getUserDataString()));
        data.addMessage(new CompilerMessage("Aldor", BuildMessage.Kind.INFO, "Preloading - build initialised"));
    }

    @SuppressWarnings("TestOnlyProblems")
    @Override
    public void discardPreloadedData(PreloadedData data) {
        LOG.info("Preload discard: " + data.getRunner() + " descriptor: " + data.getProjectDescriptor() + " " + (data.getUserDataString()));
        data.addMessage(new CompilerMessage("Aldor", BuildMessage.Kind.INFO, "Preloading - build finished"));
        BuildStaticState.instance().resetModelList();
    }

    @Override
    public @NotNull SxForm sxForm() {
        return SxFormUtils.list().add(SxFormUtils.name("AldorPreloader"));
    }
}
