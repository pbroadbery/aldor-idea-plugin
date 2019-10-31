package aldor.test_util;

public interface SafeCloseable extends AutoCloseable {
    @Override
    public void close();
}
