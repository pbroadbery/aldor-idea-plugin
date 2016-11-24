package aldor.psi;

import com.google.common.base.Strings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public final class AldorPsiUtils {
    private static final Logger LOG = Logger.getInstance(AldorPsiUtils.class);

    public static final int MAX_INDENT_DEPTH = 20;

    public static void logPsi(PsiElement psi) {
        logPsi(psi, 0);
    }

    // TODO: Remove most uses of this method
    static void logPsi(PsiElement psi, int i) {
        logPsi(psi, i, "");
    }

    static void logPsi(PsiElement psi, int depth, String lastStuff) {
        PsiElement[] children = psi.getChildren();
        int childCount = children.length;
        String text = (childCount == 0) ? psi.getText(): "";
        String spaces = Strings.repeat(" ", Math.min(depth, MAX_INDENT_DEPTH));
        if (childCount == 0) {
            System.out.println(spaces + "(psi: " + psi + " " + text + ")" + lastStuff);
            return;
        }
        System.out.println(spaces + "(psi: " + psi + " " + text);
        for (int i = 0; i < (childCount - 1); i++) {
            logPsi(children[i], depth+1, "");
        }
        logPsi(children[childCount -1], depth+1, ")" + lastStuff);
    }

    public static boolean containsElement(PsiElement element, IElementType eltType) {
        if (element.getNode().getElementType().equals(eltType)) {
            return true;
        }
        for (PsiElement child: element.getChildren()) {
            if (containsElement(child, eltType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTopLevel(PsiElement element) {
        TopLevelVisitor visitor = new TopLevelVisitor();
        element.accept(visitor);
        return visitor.isTopLevel();
    }

    private static final class TopLevelVisitor extends AldorVisitor {
        private boolean isTopLevel = false;

        @Override
        public void visitElement(PsiElement element) {
            element.getParent().accept(this);
        }

        @Override
        public void visitFile(PsiFile element) {
            isTopLevel = true;
        }

        @Override
        public void visitTopLevel(@NotNull AldorTopLevel o) {
            isTopLevel = true;
        }

        @Override
        public void visitDefine(@NotNull AldorDefine o) {
            isTopLevel = false;
        }

        @Override
        public void visitWhereRhs(@NotNull AldorWhereRhs o) {
            isTopLevel = false;
        }

        public boolean isTopLevel() {
            return isTopLevel;
        }

    }

}
