package aldor.builder.jps.autoconf.descriptors;

import aldor.util.HasSxForm;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;

public interface BuildInstanceModel extends HasSxForm {

    boolean matchesJpsModel(JpsModel model);
    JpsModule jpsModule();

    File rootDirectory();

    File targetDirectory();

    List<String> configureOptions();

    void close();

    @Nonnull
    Collection<ScriptTargetDescriptor> allScriptTargets();

    @Nonnull
    Collection<PhonyTargetDescriptor> allPhonyTargets();

    Collection<AbstractTargetDescriptor> dependencies(AbstractTargetDescriptor descriptor);
}
