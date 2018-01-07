package aldor.syntax.components;

import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

public interface NonNullProducer<T> extends Producer<T> {

    @NotNull @Override
    T produce();
}
