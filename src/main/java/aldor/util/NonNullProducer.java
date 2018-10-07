package aldor.util;

import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"InterfaceNeverImplemented", "unused"})
public interface NonNullProducer<T> extends Producer<T> {

    @NotNull @Override
    T produce();
}
