package aldor.psi;

import aldor.psi.stub.AldorDeclareStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;

public interface AldorDeclare extends StubBasedPsiElement<AldorDeclareStub> {
    //AldorDeclareStub createStub(IStubElementType<AldorDeclareStub, AldorDeclare> elementType, StubElement<?> parentStub);
    //Optional<AldorIdentifier> declareIdentifier();
    PsiElement lhs();
    //PsiElement rhs(); // not needed at the moment
}
