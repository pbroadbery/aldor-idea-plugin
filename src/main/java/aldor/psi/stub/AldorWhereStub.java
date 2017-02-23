package aldor.psi.stub;

import aldor.psi.AldorWhereBlock;
import com.intellij.psi.stubs.StubElement;

import java.util.List;

public interface AldorWhereStub extends StubElement<AldorWhereBlock> {
    List<AldorDefineStub> definitions();
}
