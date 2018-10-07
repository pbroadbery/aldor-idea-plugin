package aldor.spad;

@SuppressWarnings({"serial", "SerializableHasSerializationMethods"})
public class AldorExecutorException extends RuntimeException {

    public AldorExecutorException(String msg) {
        super(msg);
    }

    public AldorExecutorException(String s, Exception e) {
        super(s, e);
    }
}
