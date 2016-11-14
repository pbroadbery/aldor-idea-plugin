package aldor.expression;

import com.google.common.collect.Maps;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;

import java.util.Map;
import java.util.function.Supplier;

public class ExpressionTypeFactory {
    private static final ExpressionTypeFactory instance = new ExpressionTypeFactory();
    private final Map<String, Supplier<IElementType>> factoryForName = Maps.newHashMap();

    ExpressionTypeFactory() {
        factoryForName.put("DEFINE_STMT", ExpressionDefineStubElementType::new);
        factoryForName.put("FILE", FileStubElementType::new);
    }

    public static IElementType createElement(String name) {
        return instance.createElementImpl(name);
    }

    public IElementType createElementImpl(String name) {
        if (factoryForName.containsKey(name)) {
            return factoryForName.get(name).get();
        }
        return new ExpressionElementType(name);
    }

    private static final class FileStubElementType extends IStubFileElementType<PsiFileStub<PsiFile>> {
        private FileStubElementType() {
            super("expression", ExpressionLanguage.INSTANCE);
        }

    }

}
