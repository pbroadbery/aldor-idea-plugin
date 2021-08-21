package aldor.hierarchy;

import aldor.hierarchy.util.ErrorNodeDescriptor;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

public class AldorGroupedHierarchyTreeStructure extends HierarchyTreeStructure {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    //private final SmartPsiElementPointer<PsiElement> smartPointer;

    public AldorGroupedHierarchyTreeStructure(Project project, @NotNull Syntax syntax) {
        super(project, createBaseNodeDescriptor(project, syntax));
        //this.smartPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElementFromSyntax(syntax));
    }

    private static HierarchyNodeDescriptor createBaseNodeDescriptor(Project project, @NotNull Syntax syntax) {
        PsiElement element = psiElementFromSyntax(syntax);
        assert element != null; // Let's hope so anyway, otherwise go grab the index & start over.
        return new AldorHierarchyNodeDescriptor(project,  null, element, syntax, null,true);
    }

    @Override
    public boolean isAlwaysLeaf(Object element) {
        if (!(element instanceof HierarchyNodeDescriptor)) {
            return true;
        }
        HierarchyNodeDescriptor descriptor = (HierarchyNodeDescriptor) element;
        if (descriptor instanceof ErrorNodeDescriptor) {
            return true;
        }
        if (descriptor.getParentDescriptor() != null) {
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        if (descriptor instanceof ErrorNodeDescriptor) {
            return EMPTY_ARRAY;
        }
        if (descriptor.getParentDescriptor() != null) {
            return EMPTY_ARRAY;
        }

        return buildRootChildren(descriptor);
    }

    @NotNull
    private Object[] buildRootChildren(@NotNull HierarchyNodeDescriptor descriptor) {
            AldorHierarchyNodeDescriptor nodeDescriptor = (AldorHierarchyNodeDescriptor) descriptor;
        if (descriptor.getProject() == null) {
            return new Object[] { "Missing project"};
        }
        SpadLibrary library = SpadLibraryManager.getInstance(descriptor.getProject()).spadLibraryForElement(descriptor.getPsiElement());
        if (library == null) {
            return new Object[] { "Missing library"};
        }
        Syntax syntax = nodeDescriptor.syntax();

        Pair<List<SpadLibrary.ParentType>, List<SpadLibrary.Operation>> parents = library.allParents(syntax);

        Stream<Object> parentNodes = parents.getFirst().stream().map(parent -> createNodeDescriptorMaybe(nodeDescriptor, parent.type(), parent.condition()));
        Stream<Object> operationNodes = parents.getSecond().stream().map(operation -> createNodeDescriptorMaybe(nodeDescriptor, operation));

        return Stream.concat(parentNodes, operationNodes).toArray();
    }

    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, SpadLibrary.Operation operation) {
        return createOperationNodeDescriptorMaybe(parent, operation);
    }

    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, @Nonnull Syntax syntax, @Nullable Syntax condition) {
        PsiElement psiElement = psiElementFromSyntax(syntax);
        if (psiElement == null) {
            return new ErrorNodeDescriptor(parent, "Unknown element - " + syntax.getClass().getSimpleName() + " "+ SyntaxPrinter.instance().toString(syntax));
        }
        else {
            //noinspection unchecked
            return new AldorHierarchyNodeDescriptor(this.myProject, parent, psiElement, syntax, condition,false);
        }
    }

    private Object createOperationNodeDescriptorMaybe(@NotNull AldorHierarchyNodeDescriptor parent, SpadLibrary.Operation operation) {
        return new AldorHierarchyOperationDescriptor(this.myProject, parent, operation);
    }
}
