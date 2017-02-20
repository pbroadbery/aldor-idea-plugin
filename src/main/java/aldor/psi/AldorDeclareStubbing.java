package aldor.psi;

import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public final class AldorDeclareStubbing {

    public interface AldorDeclare extends StubBasedPsiElement<AldorDeclareStub> {
        //AldorDeclareStub createStub(IStubElementType<AldorDeclareStub, AldorDeclare> elementType, StubElement<?> parentStub);
        //Optional<AldorIdentifier> declareIdentifier();
        PsiElement lhs();
        //PsiElement rhs(); // not needed at the moment
    }


    public interface AldorDeclareStub extends StubElement<AldorDeclareStubbing.AldorDeclare> {

        Optional<Syntax> declareId();
        Optional<String> declareIdName();
        Syntax lhsSyntax();

        boolean isDeclareOfId();

        //AldorDefineInfo defineInfo();
    }
}
