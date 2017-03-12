package aldor.psi;

import aldor.psi.stub.AldorDeclareStub;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.annotations.Nullable;

public interface AldorDeclare extends StubBasedPsiElement<AldorDeclareStub>, NavigatablePsiElement {

    @Nullable
    AldorDeclareStub getGreenStub();

    //Optional<AldorIdentifier> declareIdentifier();
    PsiElement lhs();
    PsiElement rhs();
}
