package aldor.runconfiguration;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.testFramework.MapDataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyMapDataContext extends MapDataContext {
    @Nullable
    @Override
    public <T> T getData(@NotNull DataKey<T> key) {
        System.out.println("Get data: " +key.getName());
        return super.getData(key);
    }

    @Override
    public Object getData(@NotNull String dataId) {
        System.out.println("Get data2: " +dataId);
        return super.getData(dataId);
    }
}
