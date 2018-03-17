package aldor.hierarchy;

import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxUtils;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

public class AldorFlatHierarchyTreeStructure extends HierarchyTreeStructure {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    //private final SmartPsiElementPointer<PsiElement> smartPointer;

    public AldorFlatHierarchyTreeStructure(Project project, @NotNull Syntax syntax) {
        super(project, createBaseNodeDescriptor(project, syntax));
        //this.smartPointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElementFromSyntax(syntax));
    }

    private static HierarchyNodeDescriptor createBaseNodeDescriptor(Project project, @NotNull Syntax syntax) {
        PsiElement element = psiElementFromSyntax(syntax);
        assert element != null; // Let's hope so anyway, otherwise go grab the index & start over.
        return new AldorHierarchyNodeDescriptor(project,  null, element, syntax, true);
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

        AldorHierarchyNodeDescriptor nodeDescriptor = (AldorHierarchyNodeDescriptor) descriptor;
        SpadLibrary library = SpadLibraryManager.instance().spadLibraryForElement(descriptor.getPsiElement());
        if (library == null) {
            return new Object[] { "Missing library"};
        }
        Syntax syntax = nodeDescriptor.syntax();
        List<Syntax> parents = this.parents(library, syntax);
        //noinspection ObjectEquality
        assert parents.get(0) == syntax;
        List<SpadLibrary.Operation> operations = this.operations(library, parents);

        Stream<Object> parentNodes = parents.subList(1, parents.size()-1).stream().map(psyntax -> createNodeDescriptorMaybe(nodeDescriptor, psyntax));
        Stream<Object> operationNodes = operations.stream().map(op -> createOperationNodeDescriptorMaybe(nodeDescriptor, op));

        return Stream.concat(parentNodes, operationNodes).toArray();
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



    private List<Syntax> parents(SpadLibrary library, Syntax syntax) {
        Deque<Syntax> candidates = new ArrayDeque<>();
        candidates.add(syntax);
        List<Syntax> allParents = new ArrayList<>();
        while (!candidates.isEmpty()) {
            Syntax candidate = candidates.pop();
            if (allParents.stream().noneMatch(pp -> SyntaxUtils.match(pp, candidate))) {
                allParents.add(candidate);
                List<Syntax> parents = library.parentCategories(candidate);
                candidates.addAll(parents);
            }
        }
        return allParents;
    }

    private List<SpadLibrary.Operation> operations(SpadLibrary library, Collection<Syntax> allParents) {
        return allParents.stream().flatMap(syntax -> library.operations(syntax).stream()).collect(Collectors.toList());
    }

}
