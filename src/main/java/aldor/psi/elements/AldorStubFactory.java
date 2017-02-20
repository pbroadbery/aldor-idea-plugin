package aldor.psi.elements;

import aldor.psi.AldorDeclareStubbing.AldorDeclare;
import aldor.psi.AldorDeclareStubbing.AldorDeclareStub;
import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorDefineStubbing.AldorDefineStub;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrevStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

public interface AldorStubFactory {

    int getVersion();

    PsiStubCodec<AldorDefineStub, AldorDefine> defineCodec();

    PsiStubCodec<SpadAbbrevStub, SpadAbbrev> abbrevCodec();

    PsiStubCodec<AldorDeclareStub,AldorDeclare> declareCodec(PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory);

    interface PsiElementFactory<StubElt extends StubElement<Psi>, Psi extends PsiElement> {
        Psi invoke(StubElt stub, IStubElementType<StubElt, Psi> eltType);
    }

}
