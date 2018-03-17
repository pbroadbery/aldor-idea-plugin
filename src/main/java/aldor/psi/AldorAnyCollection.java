package aldor.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AldorAnyCollection extends ScopeFormingElement {
    @NotNull
    List<AldorIterator> getIteratorList();
}
