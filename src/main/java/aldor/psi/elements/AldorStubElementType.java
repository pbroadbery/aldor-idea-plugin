package aldor.psi.elements;

import aldor.language.AldorLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NonNls;

public abstract class AldorStubElementType<S extends StubElement<P>,
                                           P extends PsiElement,
                                           E extends IStubElementType<S, P>>
        extends StubCodecElementType<S, P, E> implements PsiElementCreator {

    protected AldorStubElementType(@NonNls String debugName, PsiStubCodec<S, P, E> codec) {
        super(debugName, AldorLanguage.INSTANCE, codec);
    }

    @Override
    @SuppressWarnings({"HardCodedStringLiteral"})
    public String toString() {
        return "{ET: " + super.toString() + "}";
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return AldorTypes.Factory.createElement(node);
    }
}
