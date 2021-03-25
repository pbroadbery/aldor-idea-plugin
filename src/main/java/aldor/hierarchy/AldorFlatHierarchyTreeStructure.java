package aldor.hierarchy;

import aldor.hierarchy.util.ErrorNodeDescriptor;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxUtils;
import aldor.util.Try;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

public class AldorFlatHierarchyTreeStructure extends HierarchyTreeStructure {
    private static final Logger LOG = Logger.getInstance(AldorFlatHierarchyTreeStructure.class);

    private static final Object[] EMPTY_ARRAY = new Object[0];
    //private final SmartPsiElementPointer<PsiElement> smartPointer;

    public AldorFlatHierarchyTreeStructure(Project project, @NotNull Syntax syntax) {
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
    public Object[] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        try {
            return buildChildren1(descriptor);
        }
        catch (ProcessCanceledException e) {
            return new Object[] {new ErrorNodeDescriptor(descriptor, "Failed to find children", e)};
        }
    }

    @NotNull
    private Object[] buildChildren1(@NotNull HierarchyNodeDescriptor descriptor) {
        if (descriptor instanceof ErrorNodeDescriptor) {
            return EMPTY_ARRAY;
        }
        if (descriptor.getParentDescriptor() != null) {
            return EMPTY_ARRAY;
        }

        if (descriptor.getProject() == null) {
            return EMPTY_ARRAY;
        }
        AldorHierarchyNodeDescriptor nodeDescriptor = (AldorHierarchyNodeDescriptor) descriptor;
        SpadLibrary library = SpadLibraryManager.getInstance(descriptor.getProject()).spadLibraryForElement(descriptor.getPsiElement());
        if (library == null) {
            return new Object[] { "Missing library"};
        }
        Syntax syntax = nodeDescriptor.syntax();
        List<Syntax> parents = this.parents(library, syntax);
        //noinspection ObjectEquality
        assert parents.get(0) == syntax;
        List<Try<SpadLibrary.Operation>> operations = this.operations(library, parents);
        LOG.info("Looking for operations: " + syntax + " parents " + parents);
        LOG.info("Looking for operations: " + operations.stream().map(x -> x.map(op -> op.name()).orElse(e -> e.getMessage())).collect(Collectors.joining(", ")));

        Stream<Object> parentNodes = parents.subList(1, parents.size()).stream().map(psyntax -> createNodeDescriptorMaybe(nodeDescriptor, psyntax, null));
        Stream<Object> operationNodes = operations.stream().map(opMaybe -> opMaybe.map(op -> createOperationNodeDescriptorMaybe(nodeDescriptor, op))
                .orElse(e -> new ErrorNodeDescriptor(nodeDescriptor, e.getMessage(), e)));

        return Stream.concat(parentNodes, operationNodes).toArray();
    }


    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, @NotNull Syntax syntax, @Nullable Syntax condition) {
        PsiElement psiElement = psiElementFromSyntax(syntax);
        if (psiElement == null) {
            return new ErrorNodeDescriptor(parent, "Unknown element - " + SyntaxPrinter.instance().toString(syntax));
        }
        else {
            //noinspection unchecked
            return new AldorHierarchyNodeDescriptor(this.myProject, parent, psiElement, syntax, condition,false);
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
                List<Syntax> parents = Try.of(() -> library.parentCategories(candidate)).orElse(e -> Collections.emptyList());
                candidates.addAll(parents);
            }
        }
        return allParents;
    }

    private List<Try<SpadLibrary.Operation>> operations(SpadLibrary library, Collection<Syntax> allParents) {
        return allParents.stream()
                .map(parent -> safeOperations(library, parent))
                .flatMap(syntaxTryList -> syntaxTryList.map(l -> l.stream().map(Try::success))
                        .orElse(e -> Stream.of(Try.failed(e))))
                .collect(Collectors.toList());
    }

    private Try<List<SpadLibrary.Operation>> safeOperations(SpadLibrary library, Syntax syntax) {
        return Try.of(() -> library.operations(syntax));
    }

}
