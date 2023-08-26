package aldor.builder.jps.autoconf.descriptors;

import aldor.util.HasSxForm;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public final class ScriptType implements HasSxForm {
    public static final ScriptType Autogen = new ScriptType(Kind.Autogen);
    public static final ScriptType Configure = new ScriptType(Kind.Configure);
    @Nullable
    private final String targetName;
    @Nullable
    private final String subdirectory;
    private final Kind kind;

    private ScriptType(Kind kind, @Nullable String targetName, @Nullable String subDirectory) {
        this.kind = kind;
        this.targetName = targetName;
        this.subdirectory = subDirectory;
    }

    private ScriptType(ScriptType.Kind kind) {
        this.kind = kind;
        this.targetName = null;
        this.subdirectory = null;
    }
    public static ScriptType makeTarget(String subDirectory, String targetName) {
        return new ScriptType(Kind.Make, targetName, subDirectory);
    }

    @Override
    @NotNull
    public SxForm sxForm() {
        SxFormUtils.SxListForm form = SxFormUtils.list().add(SxFormUtils.name("ScriptType"));
        form = form.add(SxFormUtils.name(kind.name()));
        if ((targetName != null) && (subdirectory != null)) {
            form = form.add(SxFormUtils.file(new File(subdirectory)));
            form = form.add(SxFormUtils.name(targetName));
        }
        return form;
    }

    public Kind kind() {
        return kind;
    }

    public String targetName() {
        return targetName;
    }

    public String subdirectory() {
        return subdirectory;
    }

    public String name() {
        return kind.name() + (targetName == null ? "": "-" + targetName) + (subdirectory == null ? "" : "-" + subdirectory);
    }

    @Override
    public String toString() {
        return sxForm().asSExpression();
    }

    @Nullable
    public <T> T options(ScriptOptions<T> optionName) {
        // FIXME: Should be able to add options..
        return null;
    }

    public enum Kind {
        Autogen, Configure, Make;
    }

    public static class ScriptOptions<T> {
        public static ScriptOptions<List<String>> Configure = new ScriptOptions("Configure");

        public ScriptOptions(String name) {
        }
    }
}
