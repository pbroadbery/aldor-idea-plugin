package aldor.util;

import aldor.util.sexpr.SExpression;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SExpression turned into a mapping..
 */
public class AssociationList extends AbstractMap<SExpression, SExpression> {
    private final SExpression sexpr;

    public AssociationList(SExpression sexpr) {
        this.sexpr = sexpr;
    }

    @NotNull
    @Override
    public Set<Entry<SExpression, SExpression>> entrySet() {
        return sexpr.asList().stream()
                .map(x -> new SimpleEntry<>(x.car(), x.cdr()))
                .collect(Collectors.toSet());
    }
}
