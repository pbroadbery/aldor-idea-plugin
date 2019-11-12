package aldor.runconfiguration.aldor;

import aldor.psi.AldorDefine;
import com.intellij.execution.PsiLocation;

public class AldorTestMethodLocation extends PsiLocation<AldorDefine> {

    public AldorTestMethodLocation(AldorDefine domain, AldorDefine define) {
        super(define);
    }

}
