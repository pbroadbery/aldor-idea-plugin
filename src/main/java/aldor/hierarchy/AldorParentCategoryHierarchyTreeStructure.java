package aldor.hierarchy;

import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxPsiParser.SurroundType.Leading;
import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

public final class AldorParentCategoryHierarchyTreeStructure extends HierarchyTreeStructure {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private final SmartPsiElementPointer<PsiElement> smartPointer;

    private AldorParentCategoryHierarchyTreeStructure(Project project, @NotNull Syntax syntax) {
        super(project, createBaseNodeDescriptor(project, syntax));
        this.smartPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElementFromSyntax(syntax));
    }

    public static HierarchyTreeStructure createRootTreeStructure(Project project, @NotNull PsiElement element) {
        Syntax syntax = SyntaxPsiParser.surroundingApplication(element, Leading);
        if (syntax == null) {
            return new NullHierarchyTreeStructure(element, "Invalid element - " + element);
        }
        syntax = SyntaxUtils.typeName(syntax);
        if (psiElementFromSyntax(syntax) == null) {
            return new NullHierarchyTreeStructure(element, "Failed to find syntax form for " + element.getText());
        }
        return new AldorParentCategoryHierarchyTreeStructure(project, syntax);
    }

    private static HierarchyNodeDescriptor createBaseNodeDescriptor(Project project, @NotNull Syntax syntax) {
        return new AldorHierarchyNodeDescriptor(project,  null, psiElementFromSyntax(syntax), syntax, true);
    }

    private static HierarchyNodeDescriptor createNodeDescriptor(Project project, NodeDescriptor<PsiElement> parentDescriptor, Syntax syntax) {
        return new AldorHierarchyNodeDescriptor(project,  parentDescriptor, psiElementFromSyntax(syntax), syntax, false);
    }

    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, Syntax syntax) {
        if (psiElementFromSyntax(syntax) == null) {
            return new ErrorNodeDescriptor(parent, "Unknown element - " + SyntaxPrinter.instance().toString(syntax));
        }
        else {
            return createNodeDescriptor(this.myProject, parent, syntax);
        }
    }

    private Object createOperationNodeDescriptorMaybe(@NotNull AldorHierarchyNodeDescriptor parent, SpadLibrary.Operation operation) {
        return new AldorHierarchyOperationDescriptor(this.myProject, parent, operation);
    }

    private Set<Class<?>> leafElements = Sets.newHashSet(AldorHierarchyOperationDescriptor.class, ErrorNodeDescriptor.class);

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
