package aldor.psi.stub;

import aldor.psi.SpadAbbrev;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

/**
 * Created by pab on 21/02/17.
 */
public interface SpadAbbrevStub extends StubElement<SpadAbbrev> {
    AbbrevInfo info();

    SpadAbbrev createPsi(IStubElementType<SpadAbbrevStub, SpadAbbrev> elementType);
}
