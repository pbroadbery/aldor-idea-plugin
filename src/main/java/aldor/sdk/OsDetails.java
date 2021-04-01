package aldor.sdk;

import com.intellij.openapi.util.SystemInfoRt;

public class OsDetails {
    private final boolean isWindows;

    public OsDetails() {
        this.isWindows = SystemInfoRt.isWindows;
    }

    public OsDetails(boolean isWindows) {
        this.isWindows = isWindows;
    }

    public boolean isWindows() {
        return isWindows;
    }

}
