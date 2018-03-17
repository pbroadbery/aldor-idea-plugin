package aldor.hierarchy;

import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

public final class AldorParentCategoryHierarchyTreeStructure extends HierarchyTreeStructure {
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
        return new AldorHierarchyNodeDescriptor(project,  null, Objects.requireNonNull(psiElementFromSyntax(syntax)), syntax, true);
    }

    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, Syntax syntax) {
        PsiElement psiElement = psiElementFromSyntax(syntax);
        if (psiElement == null) {
            return new ErrorNodeDescriptor(parent, "Unknown element - " + SyntaxPrinter.instance().toString(syntax));
        }
        else {
            //noinspection unchecked
            return new AldorHierarchyNodeDescriptor(this.myProject, parent, psiElement, syntax, false);
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
        AldorHierarchyNodeDescriptor aldorDescriptor = (AldorHierarchyNodeDescriptor) descriptor;
        SpadLibrary library = SpadLibraryManager.instance().spadLibraryForElement(descriptor.getPsiElement());
        if (library == null) {
            return new Object[] { "Missing library"};
        }
        Syntax syntax = aldorDescriptor.syntax();
        List<Syntax> parents = library.parentCategories(syntax);

        Stream<Object> parentNodes = parents.stream().map(psyntax -> createNodeDescriptorMaybe(aldorDescriptor, psyntax));
        Stream<Object> operationNodes = library.operations(syntax).stream().map(op -> createOperationNodeDescriptorMaybe(aldorDescriptor, op));

        return Stream.concat(parentNodes, operationNodes).toArray();
    }
}
