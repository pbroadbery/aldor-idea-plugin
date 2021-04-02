package aldor.hierarchy;

import aldor.hierarchy.util.ErrorNodeDescriptor;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.components.If;
import aldor.util.Try;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

public final class AldorParentCategoryHierarchyTreeStructure extends HierarchyTreeStructure {
    private static final Logger LOG = Logger.getInstance(AldorParentCategoryHierarchyTreeStructure.class);
    private static final Set<Class<?>> leafElements = Sets.newHashSet(AldorHierarchyOperationDescriptor.class, ErrorNodeDescriptor.class);
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private final SmartPsiElementPointer<PsiElement> smartPointer;

    public AldorParentCategoryHierarchyTreeStructure(Project project, @NotNull Syntax syntax) {
        super(project, createBaseNodeDescriptor(project, syntax));
        PsiElement psiElement = psiElementFromSyntax(syntax);
        assert psiElement != null;
        this.smartPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElement);
    }


    private static HierarchyNodeDescriptor createBaseNodeDescriptor(Project project, @NotNull Syntax syntax) {
        return new AldorHierarchyNodeDescriptor(project,  null, Objects.requireNonNull(psiElementFromSyntax(syntax)), syntax, null,true);
    }

    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, Syntax syntax) {
        Syntax theSyntax;
        @Nullable Syntax condition;
        if (syntax.is(If.class)) {
            theSyntax = syntax.as(If.class).thenPart();
            condition = syntax.as(If.class).condition();
            if (syntax.as(If.class).hasElsePart()) {
                return new ErrorNodeDescriptor(parent, "'Else' on conditions not yet supported");
            }
        } else {
            theSyntax = syntax;
            condition = null;
        }
        PsiElement psiElement = psiElementFromSyntax(theSyntax);
        if (psiElement == null) {
            return new ErrorNodeDescriptor(parent, "Unknown element - " + SyntaxPrinter.instance().toString(syntax));
        } else {
            //noinspection unchecked
            return new AldorHierarchyNodeDescriptor(this.myProject, parent, psiElement, theSyntax, condition, false);
        }
    }


    private Object createOperationNodeDescriptorMaybe(@NotNull AldorHierarchyNodeDescriptor parent, SpadLibrary.Operation operation) {
        return new AldorHierarchyOperationDescriptor(this.myProject, parent, operation);
    }

    @Override
    public boolean isAlwaysLeaf(Object element) {
        return leafElements.contains(element.getClass());
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        if (descriptor instanceof ErrorNodeDescriptor) {
            return EMPTY_ARRAY;
        }
        if (!(descriptor instanceof AldorHierarchyNodeDescriptor)) {
            return new Object[] {"Incorrect node type " + descriptor.getClass()};
        }
        if (descriptor.getProject() == null) {
            return EMPTY_ARRAY;
        }
        AldorHierarchyNodeDescriptor aldorDescriptor = (AldorHierarchyNodeDescriptor) descriptor;
        SpadLibrary library = SpadLibraryManager.getInstance(descriptor.getProject()).spadLibraryForElement(descriptor.getPsiElement());
        if (library == null) {
            return new Object[] { "Missing library"};
        }
        Syntax syntax = aldorDescriptor.syntax();
        List<Syntax> parents = Try.of(() -> library.parentCategories(syntax)).orElse(e -> Collections.emptyList());
        List<SpadLibrary.Operation> operations = Try.of(() -> library.operations(syntax))
                .peekError(t -> LOG.error("Failed to convert operations from " + syntax, t))
                .orElse(e -> Collections.emptyList());
        
        Stream<Object> parentNodes = parents.stream().map(psyntax -> createNodeDescriptorMaybe(aldorDescriptor, psyntax));
        Stream<Object> operationNodes = operations.stream().map(op -> createOperationNodeDescriptorMaybe(aldorDescriptor, op));

        return Stream.concat(parentNodes, operationNodes).toArray();
    }
}
