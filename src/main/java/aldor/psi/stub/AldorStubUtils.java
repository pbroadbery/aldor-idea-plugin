package aldor.psi.stub;

import aldor.psi.AldorWhereBlock;
import aldor.psi.elements.AldorTypes;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;

import java.util.Optional;

public final class AldorStubUtils {
    public static final StubKind<AldorDefineStub> Define = new StubKindByClass<>(AldorDefineStub.class);
    public static final StubKind<AldorDeclareStub> Declare = new StubKindByClass<>(AldorDeclareStub.class);
    public static final StubKind<EmptyStub<AldorWhereBlock>> Where = new StubKindByElementType<>(AldorTypes.WHERE_BLOCK);

    static Optional<AldorDefineStub> definingForm(StubElement<?> stub) {
        return Optional.empty();
    }


    public interface StubKind<T extends StubElement<?>> {

        boolean matches(StubElement<?> element);

        T of(StubElement<?> element);
    }

    private static class StubKindByClass<T extends StubElement<?>> implements StubKind<T> {
        private final Class<T> clss;
        StubKindByClass(Class<T> clss) {
            this.clss = clss;
        }

        @Override
        public T of(StubElement<?> element) {
            return clss.cast(element);
        }


        @Override
        public boolean matches(StubElement<?> elt) {
            return clss().isAssignableFrom(elt.getClass());
        }
        public Class<T> clss() {
            return clss;
        }

    }

    private static final class StubKindByElementType<T extends StubElement<?>> implements StubKind<T> {
        private final IElementType eltType;

        private StubKindByElementType(IElementType elementType) {
            this.eltType = elementType;
        }

        @Override
        public T of(StubElement<?> element) {
            //noinspection unchecked
            return (T) element;
        }

        @Override
        public boolean matches(StubElement<?> elt) {
            return elt.getStubType().getIndex() == eltType.getIndex();
        }
    }

/// Foo: E == I where E ==> with; I ==> add
    // Looking at with.. it has a lhs macro, so find usages.
    // If a single usage, then defining form is there
}
