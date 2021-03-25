package aldor.psi.elements;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.SpadAbbrev;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.AldorDefineStub;
import aldor.psi.stub.SpadAbbrevStub;
import aldor.psi.stub.codec.AldorDeclareStubCodec;
import aldor.psi.stub.codec.AldorDefineStubCodec;
import aldor.psi.stub.codec.SpadAbbrevStubCodec;

/**
 *
 */
public class AldorStubFactoryImpl implements AldorStubFactory {

    // Add one if any stub format is changed
    @Override
    public int getVersion() {
        return 11;
    }

    @Override
    public PsiStubCodec<AldorDefineStub, AldorDefine, AldorDefineElementType> defineCodec(PsiElementFactory<AldorDefineStub, AldorDefine> psiElementFactory) {
        return new AldorDefineStubCodec(psiElementFactory);
    }

    @Override
    public PsiStubCodec<SpadAbbrevStub, SpadAbbrev, SpadAbbrevElementType> abbrevCodec() {
        return new SpadAbbrevStubCodec();
    }

    @Override
    public PsiStubCodec<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> declareCodec(PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory) {
        return new AldorDeclareStubCodec(psiElementFactory);
    }
}
