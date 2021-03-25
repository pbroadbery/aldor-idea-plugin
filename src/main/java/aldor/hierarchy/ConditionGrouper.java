package aldor.hierarchy;

import aldor.spad.SpadLibrary;
import aldor.syntax.Syntax;
import aldor.syntax.components.If;
import aldor.util.Try;
import com.google.common.collect.Streams;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConditionGrouper {
    private final SpadLibrary library;


    public ConditionGrouper(SpadLibrary library) {
        this.library = library;
    }

    List<SyntaxWithCondition> parents(Syntax syntax) {
        Deque<SyntaxWithCondition> candidates = new ArrayDeque<>();
        candidates.add(new SyntaxWithCondition(syntax, Collections.emptyList()));
        List<SyntaxWithCondition> allParents = new ArrayList<>();
        while (!candidates.isEmpty()) {
            SyntaxWithCondition candidate = candidates.pop();
            if (matches(allParents, candidate)) {
                continue;
            }
            allParents.add(candidate);
            List<Syntax> parents = Try.of(() -> library.parentCategories(candidate.syntax)).orElse(e -> Collections.emptyList());
            List<SyntaxWithCondition> expanded = parents.stream().map(p -> p.is(If.class)
                    ? withCondition(candidate.conditions, p.as(If.class).condition(), p.as(If.class).thenPart())
                    : new SyntaxWithCondition(p, candidate.conditions)).collect(Collectors.toList());
            candidates.addAll(expanded);

        }
        return allParents;
    }

    private boolean matches(List<SyntaxWithCondition> allParents, SyntaxWithCondition candidate) {
        return allParents.stream().anyMatch(p -> p.match(candidate));
    }

    List<SpadLibrary.Operation> operations(List<SyntaxWithCondition> parents) {
        List<SpadLibrary.Operation> operations = new ArrayList<>();
        for (SyntaxWithCondition parentWithCondition: parents) {
            if (parentWithCondition.conditions.isEmpty()) {
                operations.addAll(library.operations(parentWithCondition.syntax));
            }
            else {
                operations.stream()
                        .map(op -> op.addConditions(parentWithCondition.conditions))
                        .forEach(operations::add);
            }
        }
        return operations;
    }

    private SyntaxWithCondition withCondition(List<Syntax> conditions, Syntax condition, Syntax thenPart) {
        return new SyntaxWithCondition(thenPart, Streams.concat(Stream.of(condition), conditions.stream()).collect(Collectors.toList()));
    }

    public static class SyntaxWithCondition {
        private final List<Syntax> conditions;
        private final Syntax syntax;

        private SyntaxWithCondition(Syntax syntax, List<Syntax> conditions) {
            this.conditions = conditions;
            this.syntax = syntax;
        }

        private SyntaxWithCondition(Syntax syntax, Syntax condition) {
            this(syntax, Collections.singletonList(condition));
        }


        public boolean match(SyntaxWithCondition candidate) {
            return syntax.equals(candidate.syntax) && conditions.equals(candidate.conditions);
        }

        public Syntax syntax() {
            return syntax;
        }

        List<Syntax> conditions() {
            return conditions;
        }
    }

}
