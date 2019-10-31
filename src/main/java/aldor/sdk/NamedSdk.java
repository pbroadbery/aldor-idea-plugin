package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class NamedSdk {
    @Nullable
    private final String sdkName;
    @Nullable
    private final Sdk sdk;

    public NamedSdk(@Nullable String name) {
        this.sdkName = name;
        this.sdk = null;
    }

    public NamedSdk(@NotNull Sdk sdk) {
        this.sdkName = null;
        this.sdk = sdk;
    }

    @Nullable
    public String name() {
        return (sdk == null) ? sdkName : sdk.getName();
    }

    @Nullable
    public Sdk sdk() {
        return sdk;
    }

    public static NamedSdk namedSdk(JdkComboBox.JdkComboBoxItem selection) {
        String jdkName = selection.getSdkName();
        Sdk jdk = selection.getJdk();
        return (jdk == null) ? new NamedSdk(jdkName) : new NamedSdk(jdk);

    }

    public static void initialiseJdkComboBox(@Nullable NamedSdk jdk, JdkComboBox jdkComboBox) {
        if (jdk == null) {
            jdkComboBox.setInvalidJdk("<Not Set>");
        }
        else if (jdk.sdk() == null) {
            jdkComboBox.setInvalidJdk(jdk.name());
        } else {
            jdkComboBox.setSelectedJdk(jdk.sdk());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        NamedSdk other = (NamedSdk) obj;
        return Objects.equals(this.sdk, other.sdk()) && Objects.equals(this.sdkName, other.sdkName);
    }

    @Override
    public int hashCode() {
        //noinspection ObjectInstantiationInEqualsHashCode
        return Objects.hash(sdk, sdkName);
    }
}
