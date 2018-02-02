package aldor.test_util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.util.io.AbstractStringEnumerator;
import org.jetbrains.annotations.Nullable;

public final class SimpleStringEnumerator implements AbstractStringEnumerator {
    private final BiMap<Integer, String> stringForIndex;

    public SimpleStringEnumerator() {
        this.stringForIndex = HashBiMap.create();
    }

    @Override
    public void markCorrupted() {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void force() {

    }

    @Override
    public int enumerate(@Nullable String value) {
        if (stringForIndex.inverse().containsKey(value)) {
            return stringForIndex.inverse().get(value);
        }
        int idx = stringForIndex.size() + 1;
        stringForIndex.put(idx, value);
        System.out.println("Adding: " + idx + " -> " + value);
        return idx;
    }

    @Nullable
    @Override
    public String valueOf(int idx) {
        return stringForIndex.get(idx);
    }

    @Override
    public void close() {

    }
}
