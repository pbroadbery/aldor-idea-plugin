package aldor.psi.stub;

import aldor.psi.AldorDeclare;
import aldor.syntax.Syntax;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public interface AldorDeclareStub extends StubElement<AldorDeclare> {

    Optional<Syntax> declareId();

    Optional<String> declareIdName();

    Syntax lhsSyntax();

    boolean isDeclareOfId();

    //AldorDefineInfo defineInfo();
}
