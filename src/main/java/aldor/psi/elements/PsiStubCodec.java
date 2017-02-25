package aldor.psi.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

public interface PsiStubCodec<StubElt extends StubElement<PsiElt>, PsiElt extends PsiElement, EltType extends IStubElementType<StubElt, PsiElt>> {

    void encode(StubElt stub, StubOutputStream dataStream) throws IOException;

    StubElt decode(StubInputStream dataStream, EltType eltType, StubElement<?> parentStub) throws IOException;

    PsiElt createPsi(EltType eltType, StubElt stub);

    StubElt createStub(StubElement<?> parentStub, EltType eltType, PsiElt psiElt);
}
