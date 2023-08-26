package aldor.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.TargetTypeRegistry;

@Nullable
public class TargetTypeIdentifier<T extends BuildTargetType<?>> {
    private final String id;
    private final Class<T> clzz;

    public TargetTypeIdentifier(Class<T> clzz, String id) {
        this.id = id;
        this.clzz = clzz;
    }

    @NotNull
    public T findType() {
        var targetType = TargetTypeRegistry.getInstance().getTargetType(id);
        return Classes.caster(clzz).cast(targetType).orElseThrow(() -> new IllegalStateException("No type(!)"));
    }
}
