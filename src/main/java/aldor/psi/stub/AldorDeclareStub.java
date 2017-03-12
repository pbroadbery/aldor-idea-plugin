package aldor.psi.stub;

import aldor.psi.AldorDeclare;
import aldor.syntax.Syntax;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public interface AldorDeclareStub extends StubElement<AldorDeclare> {

    Optional<String> declareIdName();

    boolean isDeclareOfId();

    boolean isCategoryDeclaration();

    Syntax declareType();

    Syntax rhsSyntax();

    Syntax syntax();

    //AldorDefineInfo defineInfo();
}
