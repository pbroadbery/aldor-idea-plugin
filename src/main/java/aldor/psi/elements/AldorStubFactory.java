package aldor.psi.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;

import java.io.IOException;

public interface AldorStubFactory {
    <PsiElt extends PsiElement, StubElt extends StubElement<PsiElt>>
    StubElt createStub(Class<PsiElt> clss, IStubElementType<StubElt, PsiElt> eltType, StubInputStream dataStream, StubElement<?> parentStub) throws IOException;

    int getVersion();
}
