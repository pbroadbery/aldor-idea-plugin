package aldor.psi;

import aldor.psi.stub.AldorDefineStub;
import com.intellij.psi.StubBasedPsiElement;

import java.util.Optional;

/**
 * Created by pab on 21/02/17.
 */
public interface AldorDefine extends StubBasedPsiElement<AldorDefineStub> {
    Optional<AldorIdentifier> defineIdentifier();
}
