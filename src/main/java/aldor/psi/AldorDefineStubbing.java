package aldor.psi;

import aldor.psi.elements.AldorDefineInfo;
import aldor.syntax.Syntax;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public final class AldorDefineStubbing {

    public interface AldorDefine extends StubBasedPsiElement<aldor.psi.AldorDefineStubbing.AldorDefineStub> {
        AldorDefineStub createStub(IStubElementType<AldorDefineStub, AldorDefine> elementType, StubElement<?> parentStub);
        Optional<AldorIdentifier> defineIdentifier();
    }


    public interface AldorDefineStub extends StubElement<AldorDefineStubbing.AldorDefine> {

        AldorDefine createPsi(IStubElementType<AldorDefineStub, AldorDefine> elementType);

        String defineId();
        Syntax syntax();

        AldorDefineInfo defineInfo();
    }
}
