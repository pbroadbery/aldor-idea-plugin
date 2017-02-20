package aldor.psi;

import aldor.psi.elements.AldorDefineInfo;
import aldor.syntax.Syntax;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public final class AldorDefineStubbing {

    public interface AldorDefine extends StubBasedPsiElement<aldor.psi.AldorDefineStubbing.AldorDefineStub> {
        Optional<AldorIdentifier> defineIdentifier();
    }


    public interface AldorDefineStub extends StubElement<AldorDefineStubbing.AldorDefine> {

        String defineId();
        Syntax syntax();

        AldorDefineInfo defineInfo();
    }
}
