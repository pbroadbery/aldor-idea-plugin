package aldor.builder.jps.autoconf.descriptors;

import aldor.util.HasSxForm;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

// Arguably, this should be an interface
public class PhonyTargetDescriptor implements AbstractTargetDescriptor, HasSxForm {
    @Nonnull
    private final String id;
    @Nonnull
    private final String presentableName;

    public PhonyTargetDescriptor(@Nonnull String id, @Nonnull String presentableName) {
        this.id = id;
        this.presentableName = presentableName;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public @NotNull SxForm sxForm() {
        return SxFormUtils.list().add(SxFormUtils.name("PhonyTargetDescriptor"))
                .add(SxFormUtils.tagged().with("Id", SxFormUtils.name(id()))
                        .with("Name", SxFormUtils.stringified(presentableName())));
    }

    public String presentableName() {
        return presentableName;
    }
}
