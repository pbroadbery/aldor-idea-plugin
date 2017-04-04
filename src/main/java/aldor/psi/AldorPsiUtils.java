package aldor.psi;

import aldor.psi.elements.AldorTypes;
import aldor.psi.impl.ReturningAldorVisitor;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.psi.SearchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;
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
        return containingBlock(aldorDeclare).type == WITH;
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

    @NotNull
    public static Optional<AldorDefine> definingForm(PsiElement element) {
        try {
            return definingForm1(element);
        }
        catch (RuntimeException ignored) {
            LOG.error("Failed to get defining form for " + element.getText());
            return Optional.empty();
        }
    }

    @NotNull
    private static Optional<AldorDefine> definingForm1(PsiElement element) {
        PsiElement form = new DefiningFormVisitor().apply(element);
        // Need to deal with macro/where here.
        if (!(form instanceof AldorDefine)) {
            return Optional.empty();
        } else {
            AldorDefine define = (AldorDefine) form;
            if (define.definitionType().equals(AldorDefine.DefinitionType.CONSTANT)) {
                return Optional.of(define);
            }
            else if (define.definitionType().equals(AldorDefine.DefinitionType.MACRO)) {
                return definitionFromMacro(define);
            }
            else {
                return Optional.empty();
            }
        }
    }

    /**
     * Handle the 'X: E == I where E ==> ...' idiom
     * @param define A macro definition (E ==> ..)
     * @return the definition where the value of the macro is used (X: E == I).
     */
    @NotNull
    private static Optional<AldorDefine> definitionFromMacro(@SuppressWarnings("TypeMayBeWeakened") AldorDefine define) {
        Iterable<PsiReference> refs = SearchUtils.findAllReferences(define);
        Iterator<PsiReference> iterator = refs.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }

        PsiReference ref = iterator.next();
        return Optional.ofNullable(PsiTreeUtil.getParentOfType(ref.getElement(), AldorDefine.class));
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

        @Override
        public void visitFile(PsiFile file) {
            returnValue(null);
        }
    }

    public interface IContainingBlockType {
        String name();
    }

    public static class ContainingBlockType<T extends PsiElement> implements IContainingBlockType {
        private final String name;
        private final Class<T> blockClass;

        public ContainingBlockType(String name, Class<T> blockClass) {
            this.name = name;
            this.blockClass = blockClass;
        }

        @Override
        public String name() {
            return name;
        }

        public Class<T> blockClass() {
            return blockClass;
        }
    }

    public static final class ContainingBlock<T extends PsiElement> {
        private final ContainingBlockType<T> type;
        private final T t;

        ContainingBlock(ContainingBlockType<T> type, T t) {
            this.type = type;
            this.t = t;
        }

        public ContainingBlockType<T> type() {
            return type;
        }

        public T element() {
            return t;
        }

        public <X extends PsiElement> Optional<ContainingBlock<X>> castTo(ContainingBlockType<X> with) {
            //noinspection unchecked,ObjectEquality
            return (this.type() == with) ? Optional.of((ContainingBlock<X>) this) : Optional.empty();

        }
    }
    public static <T extends PsiElement> ContainingBlock<T> block(ContainingBlockType<T> blockType, PsiElement element) {
        if (!blockType.blockClass().isAssignableFrom(element.getClass())) {
            throw new IllegalArgumentException(blockType.name() + " " + element);
        }
        return new ContainingBlock<T>(blockType, blockType.blockClass().cast(element));
    }

    public static final ContainingBlockType<AldorLambda> LAMBDA = new ContainingBlockType<>("Lambda", AldorLambda.class);
    public static final ContainingBlockType<AldorWith> WITH = new ContainingBlockType<>("With", AldorWith.class);
    public static final ContainingBlockType<PsiElement> ADD = new ContainingBlockType<>("Add", PsiElement.class);
    public static final ContainingBlockType<PsiFile> TOPLEVEL = new ContainingBlockType<>("TopLevel", PsiFile.class);
    public static final ContainingBlockType<AldorBlock> BODY = new ContainingBlockType<>("Block", AldorBlock.class);
    public static final ContainingBlockType<AldorWhereBlock> WHERE = new ContainingBlockType<>("Where", AldorWhereBlock.class);

    public static ContainingBlock<?> containingBlock(PsiElement elt) {
        PsiElement element = elt;
        while (element != null) {
            //noinspection ChainOfInstanceofChecks
            if (element instanceof AldorLambda) {
                return block(LAMBDA, element);
            }
            if (element instanceof AldorWith) {
                return block(WITH, element);
            }
            /*
            if (element instanceof AldorBlock) {
                return BODY.of(element);
            }
            */
            if (element instanceof AldorWhereBlock) {
                return block(WHERE, element);
            }
            if (element instanceof PsiFile) {
                return block(TOPLEVEL, element);
            }
            element = element.getParent();
        }
        throw new IllegalStateException("Missing outer block");
    }

}
