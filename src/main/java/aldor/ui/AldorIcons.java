package aldor.ui;

import com.intellij.icons.AllIcons;
import com.intellij.util.IconUtil;

import javax.swing.Icon;

public final class AldorIcons {
    // Not the best artwork in the world, but whatever...
    public static final Icon FILE = AllIcons.FileTypes.Custom;
    public static final Icon MODULE = AllIcons.FileTypes.Custom;

    public static final Icon IDENTIFIER = IconUtil.addText(IconUtil.getEmptyIcon(true), "Id");

    public static final Icon DECLARE_ICON = IconUtil.addText(IconUtil.getAddLinkIcon(), ":");

    public static final Icon MACRO = IconUtil.addText(IDENTIFIER, "M");
    public static final Icon OPERATION = IconUtil.addText(IDENTIFIER, "O");
    public static final Icon TYPE = IconUtil.addText(IDENTIFIER, "T");
}
