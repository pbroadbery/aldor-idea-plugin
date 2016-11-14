package aldor.expression;

import aldor.expression.psi.ExpressionDefine;
import com.intellij.psi.stubs.StubElement;

public interface ExpressionDefineStub extends StubElement<ExpressionDefine> {
    String type();

    String name();
}
