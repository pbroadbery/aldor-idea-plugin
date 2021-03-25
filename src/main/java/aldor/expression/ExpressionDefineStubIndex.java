package aldor.expression;

import aldor.expression.psi.ExpressionDefine;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class ExpressionDefineStubIndex extends StringStubIndexExtension<ExpressionDefine> {
    public static final StubIndexKey<String, ExpressionDefine> EXPR_DEFINE_KEY = StubIndexKey.createIndexKey("ExprDefine");
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static final ExpressionDefineStubIndex instance = new ExpressionDefineStubIndex();

    private ExpressionDefineStubIndex() {
    }


    @NotNull
    @Override
    public StubIndexKey<String, ExpressionDefine> getKey() {
        return EXPR_DEFINE_KEY;
    }


}
