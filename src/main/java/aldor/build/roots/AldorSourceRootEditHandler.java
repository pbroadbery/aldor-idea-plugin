package aldor.build.roots;

import aldor.builder.jps.AldorSourceRootProperties;
import aldor.builder.jps.AldorSourceRootType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.ui.configuration.ContentRootPanel;
import com.intellij.openapi.roots.ui.configuration.ModuleSourceRootEditHandler;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.roots.IconActionComponent;
import icons.AldorIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

public class AldorSourceRootEditHandler extends ModuleSourceRootEditHandler<AldorSourceRootProperties> {
    private static final Logger LOG = Logger.getInstance(AldorSourceRootEditHandler.class);

    protected AldorSourceRootEditHandler() {
        super(AldorSourceRootType.INSTANCE);
    }

    @Override
    @NotNull
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getRootTypeName() {
        return "Aldor Source";
    }

    @Override
    @NotNull
    public Icon getRootIcon() {
        return AldorIcons.ALDOR_FILE;
    }

    @Override
    @Nullable
    public Icon getFolderUnderRootIcon() {
        return null;
    }

    @Override
    @Nullable
    public CustomShortcutSet getMarkRootShortcutSet() {
        return null;
    }

    @Override
    @NotNull
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getRootsGroupTitle() {
        return "Aldor Source Folders";
    }

    @Override
    @NotNull
    public Color getRootsGroupColor() {
        return new JBColor(new Color(0xA0A000), new Color(0xA0A0A0));
    }

    @Override
    @NotNull
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getUnmarkRootButtonText() {
        return "Unmark Source";
    }

    @Override
    @NonNls
    @Nullable
    public JComponent createPropertiesEditor(@NotNull SourceFolder folder,
                                             @NotNull JComponent parentComponent,
                                             @NotNull ContentRootPanel.ActionCallback callback) {
        LOG.info("Creating properties editor for " + folder);
        final IconActionComponent iconComponent = new IconActionComponent(AllIcons.General.Inline_edit,
                AllIcons.General.Inline_edit_hovered,
                ProjectBundle.message("module.paths.edit.properties.tooltip"),
                () -> {
                    DialogWrapper dialog = new AldorSourceRootPropertiesDialog(parentComponent);
                    if (dialog.showAndGet()) {
                        callback.onSourceRootPropertiesChanged(folder);
                    }
                });

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(iconComponent, BorderLayout.CENTER);
        panel.add(Box.createHorizontalStrut(3), BorderLayout.EAST);
        return panel;

    }

    private static class AldorSourceRootPropertiesDialog extends DialogWrapper {
        AldorSourceRootPropertiesDialog(JComponent parentComponent) {
            super(parentComponent, true);
            setTitle("Aldor Source Properties");
            LOG.info("Creating dialog box..");
        }

        @Override
        @Nullable
        protected JComponent createCenterPanel() {
            return new JCheckBox("Test element");
        }
    }
}
