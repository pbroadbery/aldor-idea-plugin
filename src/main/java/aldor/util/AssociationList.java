package aldor.util;

import aldor.util.sexpr.SExpression;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SExpression turned into a mapping..
 */
public class AssociationList extends AbstractMap<SExpression, SExpression> {
    private final Collection<SExpression> sExpressions;

    public AssociationList(Collection<SExpression> sexpr) {
        this.sExpressions = sexpr;
    }

    @NotNull
    @Override
    public Set<Entry<SExpression, SExpression>> entrySet() {
        return sExpressions.stream()
                .map(x -> new SimpleEntry<>(x.car(), x.cdr()))
                .collect(Collectors.toSet());
    }
}
