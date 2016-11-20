package aldor.psi;

import aldor.syntax.Syntax;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public interface AldorDefine extends StubBasedPsiElement<AldorDefine.AldorDefineStub> {

    AldorDefineStub createStub(IStubElementType<AldorDefineStub, AldorDefine> elementType, StubElement<?> parentStub);

    Optional<AldorIdentifier> defineIdentifier();

    interface AldorDefineStub extends StubElement<AldorDefine> {

        AldorDefine createPsi(IStubElementType<AldorDefineStub, AldorDefine> elementType);

        String defineId();
        Syntax syntax();
    }
}
