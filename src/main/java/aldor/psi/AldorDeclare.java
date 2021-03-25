package aldor.psi;

import aldor.psi.stub.AldorDeclareStub;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("CyclicClassDependency")
public interface AldorDeclare extends StubBasedPsiElement<AldorDeclareStub>, NavigatablePsiElement, PsiNamedElement, ScopeFormingElement {

    @Nullable
    AldorDeclareStub getGreenStub();

    //Optional<AldorIdentifier> declareIdentifier();
    PsiElement lhs();
    PsiElement rhs();

    PsiElement type();
}
