package aldor.editor.finder;

import com.intellij.ide.util.ModuleRendererFactory;

import javax.swing.DefaultListCellRenderer;

/**
 * Per-module renderer for navigation items & c.. was going to use to control
 * goto class list.. but seems like a lot of work.
 * Leaving it around as it might enable some useful functionality.
 */
public class OptionRendererFactory extends ModuleRendererFactory {
    @Override
    public DefaultListCellRenderer getModuleRenderer() {
        return new DefaultListCellRenderer();
    }

    @Override
    protected boolean handles(Object element) {
        return false;
    }
}
