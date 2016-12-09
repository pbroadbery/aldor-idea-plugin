package aldor.psi.elements;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IAldorStubElementType<Stub extends StubElement<Psi>, Psi extends PsiElement> extends IStubElementType<Stub, Psi> {

    protected IAldorStubElementType(@NotNull @NonNls String debugName, @Nullable Language language) {
        super(debugName, language);
    }

    protected abstract PsiStubCodec<Stub, Psi> codec();

}
