package aldor.expression;

import aldor.expression.psi.ExpressionDefine;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

public class ExpressionDefineConcreteStub extends StubBase<ExpressionDefine> implements ExpressionDefineStub {
    private final String name;
    private final String type;

    public ExpressionDefineConcreteStub(final StubElement<?> parent, String name, String type) {
        super(parent, (IStubElementType<?, ?>) ExpressionTypes.DEFINE_STMT);
        this.name = name;
        this.type = type;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }
}
