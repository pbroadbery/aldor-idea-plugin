package aldor.util;

public final class TypedName<T> implements Name {
    private final String name;
    private final Class<T> clzz;

    public TypedName(String name, Class<T> clzz) {
        this.name = name;
        this.clzz = clzz;
    }

    public Class<T> clzz() {
        return clzz;
    }

    @Override
    public String name() {
        return name;
    }

    public static final <T> TypedName<T>  of(Class<T> clzz, String name) {
        return new TypedName<>(name, clzz);
    }

}
