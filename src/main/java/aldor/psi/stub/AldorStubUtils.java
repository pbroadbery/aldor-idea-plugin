package aldor.psi.stub;

import aldor.psi.AldorDefine;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class AldorStubUtils {

    static Optional<AldorDefineStub> definingForm(StubElement<?> stub) {
        return null;
    }


    // Foo(x: String): with == add => definingForm("x: String") == Foo.

    public enum ContainingForm {
        WITH, ADD, LHS, LAMBDA, TOP_LEVEL
    }

    public static class ExprContext {
        @Nullable
        private final AldorDefine definingForm;


        ExprContext(@Nullable AldorDefine definingForm) {
            this.definingForm = definingForm;
        }
    }

}
