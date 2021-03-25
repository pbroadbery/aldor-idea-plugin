package icons;

import com.intellij.codeInsight.template.impl.editorActions.SelectAllHandler;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconUtilEx;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IconManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.TextIcon;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.IconUIResource;
import java.awt.Color;

public final class AldorIcons {
    // Not the best artwork in the world, but whatever...
    public static final Icon ALDOR_FILE = IconLoader.getIcon("/icons/as.svg", AldorIcons.class);
    public static final Icon SPAD_FILE = IconLoader.getIcon("/icons/sp.svg", AldorIcons.class);
    public static final Icon MODULE = AllIcons.FileTypes.Custom;

    public static final Icon IDENTIFIER = IconLoader.getIcon("/icons/D-Def.svg", AldorIcons.class);

    public static final Icon DECLARE_ICON = IconUtil.addText(IconUtil.getAddLinkIcon(), ":");

    public static final Icon MACRO = IconLoader.getIcon("/icons/sp.svg", AldorIcons.class);
    public static final Icon OPERATION = IconLoader.getIcon("/icons/Fn.svg", AldorIcons.class);
    public static final Icon TYPE = IconLoader.getIcon("/icons/D-Domain.svg", AldorIcons.class);
    public static final Icon EXPORT = IconLoader.getIcon("/icons/E-Export.svg", AldorIcons.class);
    public static final Icon ParentHierarchyView = AllIcons.Hierarchy.Supertypes;
    public static final Icon GroupedHierarchyView = IconUtil.flip(AllIcons.Hierarchy.Class, false);

    /*
    public static void main(String[] args) {
        //IconManager.activate();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JBPanel c = new JBPanel<>();
        frame.add(c);
        Icon icon = new TextIcon("Hello", Color.red, Color.BLACK, 2);

        c.add(new JLabel(""+icon.getIconHeight(), icon, SwingConstants.CENTER));
        //c.add(new JLabel("!", MACRO, SwingConstants.CENTER));
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
    */

}
