package aldor.psi.elements;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.SpadAbbrev;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.AldorDefineStub;
import aldor.psi.stub.SpadAbbrevStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

public interface AldorStubFactory {

    int getVersion();

    PsiStubCodec<AldorDefineStub, AldorDefine, AldorDefineElementType> defineCodec(PsiElementFactory<AldorDefineStub, AldorDefine> psiElementFactory);

    PsiStubCodec<SpadAbbrevStub, SpadAbbrev, SpadAbbrevElementType> abbrevCodec();

    PsiStubCodec<AldorDeclareStub,AldorDeclare, AldorDeclareElementType> declareCodec(PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory);

    interface PsiElementFactory<StubElt extends StubElement<Psi>, Psi extends PsiElement> {
        Psi invoke(StubElt stub, IStubElementType<StubElt, Psi> eltType);
    }

}
