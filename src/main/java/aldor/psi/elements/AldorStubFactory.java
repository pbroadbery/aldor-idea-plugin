package aldor.psi.elements;

import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorDefineStubbing.AldorDefineStub;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrevStub;

public interface AldorStubFactory {

    int getVersion();

    PsiStubCodec<AldorDefineStub,AldorDefine> defineCodec();

    PsiStubCodec<SpadAbbrevStub, SpadAbbrev> abbrevCodec();
}
