package aldor.util;

public final class TargetTypes {
    static TargetTypes instance = new TargetTypes();

    private TargetTypes() {}

    public static TargetTypes instance() {
        return instance;
    }

}
