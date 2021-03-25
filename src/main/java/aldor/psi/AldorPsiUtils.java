package aldor.psi;

import aldor.parser.AldorParserDefinition;
import aldor.psi.elements.AldorTypes;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
        return containingBlock(aldorDeclare).type() == WITH;
    }

    private static final Set<IElementType> withElementTypes = Sets.newHashSet(AldorTypes.WITH_PART, AldorTypes.UNARY_WITH, AldorTypes.BINARY_WITH_EXPR, AldorTypes.UNARY_WITH_EXPR);
    private static final Set<IElementType> addElementTypes = Sets.newHashSet(AldorTypes.ADD_PART, AldorTypes.UNARY_ADD, AldorTypes.BINARY_ADD_EXPR, AldorTypes.UNARY_ADD_EXPR);

    private static boolean isWithElementType(IElementType stubType) {
        return withElementTypes.contains(stubType);
    }

    private static boolean isAddElementType(IElementType stubType) {
        return addElementTypes.contains(stubType);
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

    /**
     * Outermost definition containing the given element
     * @param elt an Element
     * @return the outermost definition
     */
    public static Optional<AldorDefine> topLevelDefininingForm(@NotNull PsiElement elt) {
        PsiElement current = elt;
        AldorDefine define = null;
        do {
            AldorDefine next = definingForm(current).orElse(null);
            System.out.println("conv: " + current + " --> " + next);
            if (next == null) {
                break;
            }
            current = next.getParent();
            define = next;
        } while (true);
        return Optional.ofNullable(define);
    }

    /** Find the Define element that contains this element.
     * Note that in the case of the "A: E == I where ..." idiom we should return "A: E == I" for any element in E or I.
     * @param element a starting point
     * @return the definition containing element
     */
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
        }
        else {
            AldorDefine define = (AldorDefine) form;
            switch (define.definitionType()) {
                case CONSTANT:
                    return Optional.of(define);
                case MACRO:
                    return definitionFromMacro(define);
            }
        }
        return Optional.empty();
    }

    /**
     * Handle the 'X: E == I where E ==> ...' idiom
     * @param define A macro definition (E ==> ..)
     * @return the definition where the value of the macro is used (X: E == I).
     */
    @NotNull
    private static Optional<AldorDefine> definitionFromMacro(AldorDefine define) {
        Optional<AldorWhereBlock> whereBlock = Optional.ofNullable(new WhereClauseVisitor().apply(define.getParent()));

        if (!whereBlock.isPresent()) {
            return Optional.empty();
        }
        if (!define.defineIdentifier().isPresent()) {
            return Optional.empty();
        }
        AldorIdentifier id = define.defineIdentifier().get();
        Optional<PsiElement> lhsMaybe = whereBlock.map(PsiElement::getFirstChild);

        Optional<AldorId> usages = lhsMaybe.map(Stream::of).orElse(Stream.empty())
                .flatMap(lhsElt -> PsiTreeUtil.findChildrenOfType(lhsElt, AldorId.class).stream())
                .filter(someId -> id.getText().equals(someId.getText()))
                .findFirst();
        return usages.flatMap(AldorPsiUtils::definingForm);
    }

    private static final class WhereClauseVisitor extends ReturningAldorVisitor<AldorWhereBlock> {
        private WhereClauseVisitor() {
        }

        @Override
        public void visitElement(PsiElement element) {
            element.getParent().accept(this);
        }

        @Override
        public void visitWhereBlock(@NotNull AldorWhereBlock o) {
            returnValue(o);
        }

        @Override
        public void visitFile(PsiFile file) {
            returnValue(null);
        }

        @Override
        public void visitDefine(@NotNull AldorDefine ignored) {
            returnValue(null);
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
        return new ContainingBlock<>(blockType, blockType.blockClass().cast(element));
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

    public enum DefinitionClass {
        CATEGORY, DOMAIN, VALUE
    }

    public static DefinitionClass definitionClassForDefine(AldorDefine define) {
        if (isCategoryDefinition(define)) {
            return DefinitionClass.CATEGORY;
        }
        if (isDomainByLhs(define)) {
            return DefinitionClass.DOMAIN;
        }
        if (isDomainByRhs(define)) {
            return DefinitionClass.DOMAIN;
        }
        return DefinitionClass.VALUE;
    }

    static boolean isCategoryDefinition(AldorDefine define) {
        // foo: Category == ...
        AldorDeclare decl = PsiTreeUtil.findChildOfType(define.lhs(), AldorDeclare.class);
        if (decl == null) {
            return false;
        }
        AldorIdentifier ident = PsiTreeUtil.findChildOfType(decl.rhs(), AldorIdentifier.class);
        if (ident != null) {
            if ("Category".equals(ident.getText())) {
                return true;
            }
        }
        return false;
    }

    static boolean isDomainByLhs(AldorDefine define) {
        AldorDeclare decl = PsiTreeUtil.findChildOfType(define.lhs(), AldorDeclare.class);
        if (decl == null) {
            return false;
        }
        if (PsiTreeUtil.findChildOfType(decl.rhs(), AldorWith.class) != null) {
            return true;
        }
        return false;
    }

    static boolean isDomainByRhs(AldorDefine define) {
        // foo: ... == add
        PsiElement rhs = define.rhs();
        if (PsiTreeUtil.findChildOfAnyType(define.rhs(), AldorUnaryAdd.class, AldorUnaryAddExpr.class, AldorAddPart.class, AldorBinaryAddExpr.class) != null) {
            return true;
        }
        return false;
    }

    @NotNull
    public static List<Binding> childBindings(@NotNull PsiElement elt) {
        return (new BindingSearchVisitor().apply(elt));
    }

    public static class Binding {
        private final PsiElement element;

        Binding(PsiElement element) {
            this.element = element;
        }

        public PsiElement element() {
            return element;
        }

        public <T> Optional<T> maybeAs(Class<T> clss) {
            return Optional.of(element).flatMap(e -> clss.isAssignableFrom(element.getClass())
                    ? Optional.of(clss.cast(element))
                    : Optional.empty());
        }
    }

    public static class BindingSearchVisitor extends CollectingAldorVisitor<Binding> {

        @Override
        public void visitElement(PsiElement element) {
            element.acceptChildren(this);
        }

        @Override
        public void visitDefine(@NotNull AldorDefine o) {
            add(new Binding(o));
        }

        @Override
        public void visitDeclare(@NotNull AldorDeclare o) {
            add(new Binding(o));
        }

        @Override
        public void visitPrimaryExpr(@NotNull AldorPrimaryExpr o) {
        }

        @Override
        public void visitWhereBlock(@NotNull AldorWhereBlock o) {
        }
    }


    public static Optional<AldorId> findUniqueIdentifier(PsiElement element) {
        return new UniqueIdVisitor<>(AldorId.class).apply(element);
    }

    private static class UniqueIdVisitor<T> extends AldorVisitor {
        private final Class<T> clzz;
        private T value = null;

        UniqueIdVisitor(Class<T> clzz) {
            this.clzz = clzz;
        }

        public Optional<T> apply(PsiElement element) {
            element.accept(this);
            return Optional.ofNullable(value);
        }

        @Override
        public void visitElement(@NotNull PsiElement o) {
            if (clzz.isAssignableFrom(o.getClass())) {
                //noinspection unchecked
                value = (T) o;
                return;
            }

            @Nullable PsiElement child = null;
            boolean found = false;
            ASTNode childNode = o.getNode().getFirstChildNode();
            while (childNode != null) {
                if (!isWhitespaceOrComment(childNode) && (childNode.getTextLength() != 0)) {
                    if (found) {
                        child = null;
                        break;
                    } else {
                        PsiElement e = childNode.getPsi();
                        found = true;
                        child = e;
                    }
                }
                childNode = childNode.getTreeNext();
            }
            if (child != null) {
                child.accept(this);
            }
        }

    }

    public static boolean isWhitespaceOrComment(@NotNull PsiElement element) {
        return isWhitespaceOrComment(element.getNode());
    }

    public static boolean isWhitespaceOrComment(@NotNull ASTNode node) {
        IElementType elementType = node.getElementType();
        return AldorParserDefinition.WHITE_SPACES.contains(elementType)
                || AldorParserDefinition.DOC_TOKENS.contains(elementType);
    }

}
