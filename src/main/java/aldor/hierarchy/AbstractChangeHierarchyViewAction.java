package aldor.hierarchy;

import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public abstract class AbstractChangeHierarchyViewAction extends ToggleAction {

    AbstractChangeHierarchyViewAction(final String shortDescription, final String longDescription, final Icon icon) {
        super(shortDescription, longDescription, icon);
    }

    @Override
    public final boolean isSelected(final AnActionEvent event) {
        final AldorTypeHierarchyBrowser browser = (AldorTypeHierarchyBrowser) getTypeHierarchyBrowser(event.getDataContext());
        return (browser != null) && getTypeName().equals(browser.typeName());
    }

    protected abstract String getTypeName();

    @Override
    public final void setSelected(final AnActionEvent event, final boolean flag) {
        if (flag) {
            final TypeHierarchyBrowserBase browser = getTypeHierarchyBrowser(event.getDataContext());
            //        setWaitCursor();
            ApplicationManager.getApplication().invokeLater(() -> {
                if (browser != null) {
                    browser.changeView(getTypeName());
                }
            });
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        // its important to assign the myTypeHierarchyBrowser first
        super.update(event);
        final Presentation presentation = event.getPresentation();
        presentation.setEnabled(true);
    }

    static TypeHierarchyBrowserBase getTypeHierarchyBrowser(final DataContext context) {
        return TypeHierarchyBrowserBase.DATA_KEY.getData(context);
    }


}
