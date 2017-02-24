package aldor.psi.elements;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorWhereBlock;
import aldor.psi.SpadAbbrev;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.AldorDefineStub;
import aldor.psi.stub.AldorWhereStub;
import aldor.psi.stub.SpadAbbrevStub;

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

    @Override
    public PsiStubCodec<AldorWhereStub, AldorWhereBlock> whereCodec() {
        return new AldorWhereCodec();
    }

}
