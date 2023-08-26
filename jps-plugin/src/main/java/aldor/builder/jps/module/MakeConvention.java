package aldor.builder.jps.module;

public enum MakeConvention {
    None,
    Source, // makefile is in the source directory - output to {outdir}/{rel}/foo.ao, target is {rel}/foo.ao
    Build, // makefile is in the build directory
    Configured // autoconf conventions
    ;

    public boolean enabled() {
        return this != None;
    }
}
