package aldor.formatting;

import aldor.file.AldorFileType;
import aldor.language.AldorLanguage;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Random style settings for Aldor.
 * Can't think of much at the moment...
 */
public class AldorCodeStylePanel extends CodeStyleAbstractPanel {

    public static final String ALIGNMENT_SAMPLE = "THINK OF SOMETHING FUN TO WRITE HERE";
    private static final int RIGHT_MARGIN_DEFAULT = 80;
    private JPanel myPreviewPanel;
    private JPanel myPanel;
    private JCheckBox useLigatures;

    @SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
    public AldorCodeStylePanel(@NotNull CodeStyleSettings settings) {
        super(AldorLanguage.INSTANCE, null, settings);
        addPanelToWatch(myPanel);
        addPanelToWatch(myPanel);
        installPreviewPanel(myPreviewPanel);
    }

    @Override
    protected int getRightMargin() {
        return RIGHT_MARGIN_DEFAULT;
    }

    @Nullable
    @Override
    protected EditorHighlighter createHighlighter(EditorColorsScheme scheme) {
        return EditorHighlighterFactory.getInstance().createEditorHighlighter(new LightVirtualFile("a.as"), scheme, null);
    }

    @NotNull
    @Override
    protected FileType getFileType() {
        return AldorFileType.INSTANCE;
    }

    @Nullable
    @Override
    protected String getPreviewText() {
        return ALIGNMENT_SAMPLE;
    }

    @Override
    public void apply(CodeStyleSettings settings) throws ConfigurationException {
    }

    @Override
    public boolean isModified(CodeStyleSettings settings) {
        return false;
    }

    @Nullable
    @Override
    public JComponent getPanel() {
        return myPanel;
    }

    @Override
    protected void resetImpl(CodeStyleSettings settings) {
    }

}
