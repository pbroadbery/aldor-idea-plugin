package aldor.module.template.detect;

import aldor.build.facet.cfgroot.ConfigRootConfigurable;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

class ConfigurationWizardConfigurable {
    private static final Logger LOG = Logger.getInstance(ConfigurationWizardConfigurable.class);

    private JTabbedPane tabbedPane = null;
    private Map<File, ConfigRootConfigurable> tabsForDirectory = null;

    ConfigurationWizardConfigurable() {
        LOG.info("Constructor");
        tabbedPane = new JBTabbedPane();
        tabbedPane.add("None", createDefaultTab());
    }

    public @Nullable JComponent getComponent() {
        LOG.info("getComponent");
        return tabbedPane;
    }

    public DetectedRootFacetSettings currentState() {
        if (tabsForDirectory == null) {
            LOG.info("Current state-  Init not called");
            return new DetectedRootFacetSettings();
        }
        var settings = new DetectedRootFacetSettings();
        for (var ent : tabsForDirectory.entrySet()) {
            ent.getValue().apply();
            settings.put(ent.getKey(), ent.getValue().currentState());
        }
        LOG.info("Returning settings: " + settings.isEmpty());
        return settings;
    }

    public void initialise(DetectedRootFacetSettings settings) {
        LOG.info("reset " + settings.isEmpty());
        tabsForDirectory = new HashMap<>();
        if ((settings.isEmpty()) && (tabbedPane.getTabCount() == 1) && "None".equals(tabbedPane.getTitleAt(0))) {
            LOG.info("reset::No tabs required");
            return;
        }
        tabbedPane.removeAll();
        if (settings.isEmpty()) {
            tabbedPane.addTab("None", createDefaultTab());
        }
        int count = 1;
        for (var propertyEnt : settings.facetPropertiesForRoot().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            File directory = propertyEnt.getKey();
            var properties = propertyEnt.getValue();
            var configurable = createConfigTab(properties);

            tabbedPane.addTab("Repository #" + count + " " + directory, configurable.createComponent());
            tabsForDirectory.put(directory, configurable);
            count++;
        }
        LOG.info("reset::Initialised with " + (count - 1) + " tabs");
    }

    private ConfigRootConfigurable createConfigTab(ConfigRootFacetProperties settings) {
        LOG.info("createConfigTab");
        return new ConfigRootConfigurable(settings);
    }

    public boolean validate() {
        LOG.info("validate");
        return true;
    }

    @NotNull
    private static JPanel createDefaultTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("[Aldor/Fricas] No configured repositories found");
        panel.add(label,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));
        panel.add(new JLabel("<<placeholder>>"),
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));
        panel.add(new JPanel(),
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 1.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));
        return panel;
    }

    public void disposeUIResources() {
        //tabbedPane
        if (tabsForDirectory == null) {
            return;
        }
        for (var cfg : tabsForDirectory.values()) {
            cfg.disposeUIResources();
        }
    }
}
