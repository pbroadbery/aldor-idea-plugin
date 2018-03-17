package aldor.psi.stub;

import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorDefineInfo;
import aldor.syntax.Syntax;
import com.intellij.psi.stubs.StubElement;

@SuppressWarnings("CyclicClassDependency")
public interface AldorDefineStub extends StubElement<AldorDefine> {

    String defineId();

    Syntax syntax();

    AldorDefineInfo defineInfo();
}
