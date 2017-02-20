package aldor.psi.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

public interface PsiStubCodec<StubElt extends StubElement<PsiElt>, PsiElt extends PsiElement> {

    void encode(StubElt stub, StubOutputStream dataStream) throws IOException;

    StubElt decode(StubInputStream dataStream, IStubElementType<StubElt, PsiElt> eltType, StubElement<?> parentStub) throws IOException;

    PsiElt createPsi(IStubElementType<StubElt, PsiElt> eltType, StubElt stub);

    StubElt createStub(StubElement<?> parentStub, IStubElementType<StubElt, PsiElt> eltType, PsiElt psiElt);
}
