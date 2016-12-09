package aldor.psi.elements;

import aldor.psi.AldorDefineStubbing;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrevStub;

public interface AldorStubFactory {

    int getVersion();

    PsiStubCodec<AldorDefineStubbing.AldorDefineStub,AldorDefineStubbing.AldorDefine> defineCodec();

    PsiStubCodec<SpadAbbrevStub, SpadAbbrev> abbrevCodec();
}
