package aldor.psi;

import aldor.psi.stub.AldorDefineStub;
import com.intellij.pom.Navigatable;
import com.intellij.psi.StubBasedPsiElement;

import java.util.Optional;

public interface AldorDefine extends StubBasedPsiElement<AldorDefineStub>, Navigatable {
    Optional<AldorIdentifier> defineIdentifier();
    DefinitionType definitionType();

    enum DefinitionType { CONSTANT, MACRO }
}
