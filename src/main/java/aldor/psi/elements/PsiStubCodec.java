package aldor.psi.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

public interface PsiStubCodec<StubElt extends StubElement<PsiElt>, PsiElt extends PsiElement> {
        void encode(StubElt stub, StubOutputStream dataStream) throws IOException;

        StubElt decode(IStubElementType<StubElt, PsiElt> eltType,
                       StubInputStream dataStream, StubElement<?> parentStub) throws IOException;
}
