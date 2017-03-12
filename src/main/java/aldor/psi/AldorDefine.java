package aldor.psi;

import aldor.psi.stub.AldorDefineStub;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;

import java.util.Optional;

public interface AldorDefine extends StubBasedPsiElement<AldorDefineStub>, NavigatablePsiElement {
    Optional<AldorIdentifier> defineIdentifier();
    DefinitionType definitionType();

    default PsiElement lhs() {
        return getFirstChild();
    }

    enum DefinitionType { CONSTANT, MACRO }
}
