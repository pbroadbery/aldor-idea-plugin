package aldor.module.template;

import java.util.function.Function;

public enum GitModuleType {
    Aldor((b -> b.new AldorGitModuleDetail())),
    Fricas(b-> b.new FricasGitModuleDetail());

    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private final Function<AldorGitModuleBuilder, GitModuleDetail> fn;

    GitModuleType(Function<AldorGitModuleBuilder, GitModuleDetail> fn) {
        this.fn = fn;
    }

    public Function<AldorGitModuleBuilder, GitModuleDetail> fn() {
        return fn;
    }
}
