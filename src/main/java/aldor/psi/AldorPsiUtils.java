package aldor.psi;

import aldor.psi.elements.AldorTypes;
import aldor.psi.impl.ReturningAldorVisitor;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class AldorPsiUtils {
    private static final Logger LOG = Logger.getInstance(AldorPsiUtils.class);

    public static final int MAX_INDENT_DEPTH = 20;

    public static void logPsi(PsiElement psi) {
        logPsi(psi, 0, "");
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

    public static boolean isCategoryDeclaration(@SuppressWarnings("TypeMayBeWeakened") AldorDeclare aldorDeclare) {
        //noinspection ObjectEquality
        return containingBlock(aldorDeclare) == WITH;
    }

    private static final Set<IElementType> withElementTypes = Sets.newHashSet(AldorTypes.WITH_PART, AldorTypes.UNARY_WITH, AldorTypes.BINARY_WITH_EXPR, AldorTypes.UNARY_WITH_EXPR);

    private static boolean isWithElementType(IStubElementType<?, ?> stubType) {
        return withElementTypes.contains(stubType);
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

        // until exported macros work...
        @Override
        public void visitMacroBody(@NotNull AldorMacroBody e) { isTopLevel = false; }

        public boolean isTopLevel() {
            return isTopLevel;
        }
    }

    public AldorDefine aldorDefiningForm(PsiElement element) {
        PsiElement form = new DefiningFormVisitor().apply(element);
        if (form instanceof AldorDefine) {
            return (AldorDefine) form;
        }
        else {
            return null;
        }
    }

    private static final class DefiningFormVisitor extends ReturningAldorVisitor<PsiElement> {

        private DefiningFormVisitor() {
        }

        @Override
        public void visitElement(PsiElement element) {
            element.getParent().accept(this);
        }

        @Override
        public void visitDefine(@NotNull AldorDefine o) {
            returnValue(o);
        }

        @Override
        public void visitMacroBody(@NotNull AldorMacroBody o) {
            returnValue(o);
        }

        @Override
        public void visitLambda(@NotNull AldorLambda o) {
            returnValue(o);
        }

        @Override
        public void visitWhereRhs(@NotNull AldorWhereRhs o) {
            returnValue(null);
        }
    }

    public static class ContainingBlockType<T> {
        private final String name;
        private final Class<T> blockClass;

        public ContainingBlockType(String name, Class<T> blockClass) {
            this.name = name;
            this.blockClass = blockClass;
        }
    }

    private static final class ContainingBlock<T> {
        private final ContainingBlockType<T> type;
        private final T t;

        ContainingBlock(ContainingBlockType<T> type, T t) {
            this.type = type;
            this.t = t;
        }
    }

    private static final ContainingBlockType<AldorLambda> LAMBDA = new ContainingBlockType<>("Lambda", AldorLambda.class);
    private static final ContainingBlockType<AldorWith> WITH = new ContainingBlockType<>("With", AldorWith.class);
    private static final ContainingBlockType<PsiElement> ADD = new ContainingBlockType<>("Add", PsiElement.class);
    private static final ContainingBlockType<PsiFile> TOPLEVEL = new ContainingBlockType<>("TopLevel", PsiFile.class);
    private static final ContainingBlockType<AldorBlock> BODY = new ContainingBlockType<>("Block", AldorBlock.class);
    private static final ContainingBlockType<AldorBlock> WHERE = new ContainingBlockType<>("Where", AldorBlock.class);

    public static ContainingBlockType<?> containingBlock(PsiElement elt) {
        PsiElement element = elt;
        while (element != null) {
            if (element instanceof AldorLambda) {
                return LAMBDA;
            }
            if (element instanceof AldorWith) {
                return WITH;
            }
            if (element instanceof AldorDefine) {
                return BODY;
            }
            element = element.getParent();
        }
        return TOPLEVEL;
    }

}
