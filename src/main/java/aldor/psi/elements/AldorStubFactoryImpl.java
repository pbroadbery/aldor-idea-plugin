package aldor.psi.elements;

import aldor.psi.AldorDeclareStubbing.AldorDeclare;
import aldor.psi.AldorDeclareStubbing.AldorDeclareStub;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrevStub;

import static aldor.psi.AldorDefineStubbing.AldorDefine;
import static aldor.psi.AldorDefineStubbing.AldorDefineStub;

/**
 *
 */
public class AldorStubFactoryImpl implements AldorStubFactory {

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public PsiStubCodec<AldorDefineStub, AldorDefine> defineCodec() {
        return new AldorDefineStubCodec();
    }

    @Override
    public PsiStubCodec<SpadAbbrevStub, SpadAbbrev> abbrevCodec() {
        return new SpadAbbrevStubCodec();
    }

    @Override
    public PsiStubCodec<AldorDeclareStub, AldorDeclare> declareCodec(PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory) {
        return new AldorDeclareStubCodec(psiElementFactory);
    }

}
