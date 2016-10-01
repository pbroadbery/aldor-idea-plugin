package aldor.util;

import java.util.AbstractMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by pab on 01/10/16.
 */
public class AssociationList extends AbstractMap<SExpression, SExpression> {
    SExpression sexpr;

    public AssociationList(SExpression sexpr) {
        this.sexpr = sexpr;
    }

    @Override
    public Set<Entry<SExpression, SExpression>> entrySet() {

        return sexpr.asList().stream()
                .map(x -> new SimpleEntry<SExpression, SExpression>(x.car(), x.cdr()))
                .collect(Collectors.toSet());
    }
}
