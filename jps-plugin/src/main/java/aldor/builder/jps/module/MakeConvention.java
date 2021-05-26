package aldor.builder.jps.module;

public enum MakeConvention {
    Source, // makefile is in the source directory - build in the same location?
    Build, // makefile is in the build directory
    Configured // autoconf conventions
}
