package aldor.build.facet.cfgroot;

import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.util.TypedTry;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.function.Function;

import static com.google.common.base.Strings.emptyToNull;

public class ConfigRootConfigurable implements UnnamedConfigurable {
    private static final Logger LOG = Logger.getInstance(ConfigRootConfigurable.class);
    private final ConfigState state;
    private CommittableFieldPanel myBuildPathPanel = null;
    private CommittableFieldPanel myInstallPathPanel = null;

    public ConfigRootConfigurable(ConfigRootFacetProperties initial) {
        this.state = new ConfigState(initial);
    }

    @Override
    public boolean isModified() {
        return !currentState().equals(extractState(currentState()));
    }

    @Override
    public void apply() {
        state.update(p -> extractState(p));
    }

    private ConfigRootFacetProperties extractState(ConfigRootFacetProperties p) {
        return p.asBuilder()
                .setBuildDirectory(emptyToNull(myBuildPathPanel.getText()))
                .setInstallDirectory(emptyToNull(myInstallPathPanel.getText())).build();
    }

    @Override
    public void reset() {
        this.myBuildPathPanel.setText(currentState().buildDirectory());
        this.myInstallPathPanel.setText(currentState().installDirectory());
    }

    @Override
    @NotNull
    public JComponent createComponent() {
        final JPanel panel = new JPanel(new GridBagLayout());
        //panel.setBorder(new EmptyBorder(UIUtil.PANEL_SMALL_INSETS));
        panel.setBorder(new LineBorder(JBColor.RED, 3));
        JLabel label = new JLabel("This is a configuration step");
        panel.add(label,
                new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));
        JLabel buildLabel = new JLabel("Build:");
        JLabel installLabel = new JLabel("Install:");
        myBuildPathPanel = createOutputPathPanel("Build", "Build Location title", url -> setBuildLocation(url));
        myInstallPathPanel = createOutputPathPanel("Install", "Install Location title", url -> setInstallLocation(url));
        buildLabel.setLabelFor(myBuildPathPanel.getTextField());
        installLabel.setLabelFor(myInstallPathPanel.getTextField());

        panel.add(buildLabel,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));

        panel.add(myBuildPathPanel,
                new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));
        panel.add(installLabel,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));

        panel.add(myInstallPathPanel,
                new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insets(8, 10, 0, 10), 0, 0));
        // todo: List of options (java, gmp, etc), plus configure args
        // Extra panel at the bottom to use all the space
        JPanel base = new JPanel();
        panel.add(base, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                JBUI.insets(8, 10, 0, 10), 0, 0));
        updateComponents();
        return panel;
    }

    private void setBuildLocation(String url) {
        //this.state.currentState = this.state.currentState.asBuilder().setBuildDirectory(url).build();
    }

    private void setInstallLocation(String url) {
        //this.state.currentState = this.state.currentState.asBuilder().setInstallDirectory(url).build();
    }

    private CommittableFieldPanel createOutputPathPanel(final String boxName, final String title, final OnCommitRunnable commitPathRunnable) {
        final JTextField textField = new ExtendableTextField();
        textField.setName(boxName);
        final FileChooserDescriptor outputPathsChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        //outputPathsChooserDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, getModel().getModule());
        outputPathsChooserDescriptor.setHideIgnored(false);
        InsertPathAction.addTo(textField, outputPathsChooserDescriptor);
        FileChooserFactory.getInstance().installFileCompletion(textField, outputPathsChooserDescriptor, true, null);
        OnCommitRunnable commitConsumer = v -> {
            LOG.info("Saving value: " + (v == null ? "<null>" : v));
            commitPathRunnable.saveURL(v);
        };
        final Runnable commitRunnable = () -> {
            final String path = textField.getText().trim();
            if (path.isEmpty()) {
                commitConsumer.saveURL(null);
            } else {
                // should set only absolute paths
                String canonicalPath = TypedTry.of(IOException.class, () -> FileUtil.resolveShortWindowsName(path)).orElse(path);
                commitConsumer.saveURL(canonicalPath);
            }
        };

        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                commitRunnable.run();
            }
        });

        BrowseFilesListener browseButtonActionListener = new BrowseFilesListener(textField, title, "", outputPathsChooserDescriptor) {
            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                commitRunnable.run();
            }
        };
        return new CommittableFieldPanel(textField, browseButtonActionListener, null, commitRunnable);
    }

    public ConfigRootFacetProperties currentState() {
        return state.currentState;
    }

    public ConfigRootFacetProperties initialState() {
        return state.initialState;
    }

    private void updateComponents() {
    }


    private interface OnCommitRunnable {
        // NB: Implementations Will need a 'getModel().isWritable())' check
        void saveURL(String url);
    }

    private static class CommittableFieldPanel extends FieldPanel {
        private final Runnable myCommitRunnable;

        CommittableFieldPanel(final JTextField textField,
                              ActionListener browseButtonActionListener,
                              final Runnable documentListener,
                              final Runnable commitPathRunnable) {
            super(textField, null, null, browseButtonActionListener, documentListener);
            myCommitRunnable = commitPathRunnable;
        }

        public void commit() {
            myCommitRunnable.run();
        }
    }

    static class ConfigState {
        ConfigRootFacetProperties initialState;
        ConfigRootFacetProperties currentState;

        ConfigState(ConfigRootFacetProperties properties) {
            this.initialState = properties;
            this.currentState = properties.copy();
        }

        public boolean isModified() {
            return !initialState.equals(currentState);
        }

        void update(Function<ConfigRootFacetProperties, ConfigRootFacetProperties> fn) {
            this.currentState = fn.apply(currentState);
        }

        public ConfigRootFacetProperties current() {
            return currentState;
        }

        public void reset() {
            this.currentState = initialState;
        }
    }
}