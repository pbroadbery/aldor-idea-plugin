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
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;
import static java.util.Objects.requireNonNull;

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
        List<Syntax> parents = this.parents(library, syntax);
        //noinspection ObjectEquality
        assert parents.get(0) == syntax;
        List<Grouping> groupings = this.operations(library, parents);

        Stream<Object> parentNodes = parents.subList(1, parents.size()).stream().map(psyntax -> createNodeDescriptorMaybe(nodeDescriptor, psyntax));
        Stream<Object> operationNodes = groupings.stream().map(grp -> createNodeDescriptorMaybe(nodeDescriptor, grp));

        return Stream.concat(parentNodes, operationNodes).toArray();
    }

    private Object createNodeDescriptorMaybe(AldorHierarchyNodeDescriptor parent, Grouping grouping) {
        if (grouping.operations().size() == 1) {
            return createOperationNodeDescriptorMaybe(parent, grouping.operations().get(0));
        }
        else {
            return new GroupingHierarchyDescriptor(parent, requireNonNull(parent.getPsiElement()), grouping);
        }
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
                List<Syntax> parents = Try.of(() -> library.parentCategories(candidate)).orElse(e -> Collections.emptyList());
                candidates.addAll(parents);
            }
        }
        return allParents;
    }

    private List<Grouping> operations(SpadLibrary library, Collection<Syntax> allParents) {
        Stream<SpadLibrary.Operation> operations = allParents.stream().flatMap(syntax -> safeOperations(library, syntax).stream());
        Map<GroupingKey, List<SpadLibrary.Operation>> collected = operations.collect(Collectors.groupingBy(this::groupingKey));

        return collected.entrySet().stream().map(x -> new Grouping(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    private List<SpadLibrary.Operation> safeOperations(SpadLibrary library, Syntax syntax) {
        return Try.of(() -> library.operations(syntax)).orElse(e -> Collections.emptyList());
    }

    GroupingKey groupingKey(SpadLibrary.Operation op) {
        return new GroupingKey(op.name(), op.type());
    }

    public static class GroupingKey {
        private final String name;
        private final Syntax type;

        GroupingKey(String name, Syntax type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != getClass()) {
                return false;
            }
            GroupingKey other = (GroupingKey) obj;

            return name.equals(other.name()) && SyntaxUtils.match(other.type(), type);
        }

        public String name() {
            return name;
        }

        public Syntax type() {
            return type;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    static class Grouping {
        private final GroupingKey key;
        private final List<SpadLibrary.Operation> operations;

        Grouping(GroupingKey type, List<SpadLibrary.Operation> operations) {
            this.key = type;
            this.operations = operations;
        }

        public GroupingKey key() {
            return key;
        }

        public List<SpadLibrary.Operation> operations() {
            return operations;
        }
    }

}
