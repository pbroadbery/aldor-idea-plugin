package aldor.sdk.fricas;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class FricasSdkDataConfigurable implements AdditionalDataConfigurable {

    @Override
    public void setSdk(@Nullable Sdk sdk) {
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return new JPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }
}
