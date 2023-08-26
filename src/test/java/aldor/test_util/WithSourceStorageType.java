package aldor.test_util;

public class WithSourceStorageType extends DelegatingDescriptor {
    private final SourceFileStorageType sourceFileStorageType;

    WithSourceStorageType(SdkDescriptor descriptor, SourceFileStorageType sourceFileStorageType) {
        super(descriptor);
        this.sourceFileStorageType = sourceFileStorageType;
    }

    @Override
    public String name(String prefix) {
        return super.name(prefix) + "_" + sourceFileStorageType;
    }

    @Override
    public SourceFileStorageType sourceFileType() {
        return SourceFileStorageType.Real;
    }
}
