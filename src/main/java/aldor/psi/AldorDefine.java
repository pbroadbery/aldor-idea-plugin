package aldor.psi;

import aldor.psi.stub.AldorDefineStub;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface AldorDefine extends StubBasedPsiElement<AldorDefineStub>, PsiNameIdentifierOwner, NavigatablePsiElement {
    @NotNull
    Optional<AldorIdentifier> defineIdentifier();
    DefinitionType definitionType();

    default PsiElement lhs() {
        return getFirstChild();
    }

    default PsiElement rhs() {
        return getLastChild();
    }

    enum DefinitionType { CONSTANT, MACRO }
}
