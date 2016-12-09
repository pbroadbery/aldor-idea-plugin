package aldor.psi.elements;

import aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrevStub;

import static aldor.psi.AldorDefineStubbing.AldorDefine;
import static aldor.psi.AldorDefineStubbing.AldorDefineStub;

/**
 *
 */
public class AldorStubFactoryImpl implements AldorStubFactory {
    private static final PsiStubCodec<AldorDefineStub, AldorDefine> defineCodec = new AldorDefineStubCodec();
    private static final PsiStubCodec<SpadAbbrevStub, SpadAbbrev> abbrevCodec = new SpadAbbrevStubCodec();

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public PsiStubCodec<AldorDefineStub, AldorDefine> defineCodec() {
        return defineCodec;
    }

    @Override
    public PsiStubCodec<SpadAbbrevStub, SpadAbbrev> abbrevCodec() {
        return abbrevCodec;
    }
}
