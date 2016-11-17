package aldor.psi.elements;

import aldor.language.AldorLanguage;
import com.google.common.collect.Maps;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;

import java.util.Map;

/**
 * Aldor element types
 */
public class AldorElementTypeFactory {
    private static final AldorElementTypeFactory instance = new AldorElementTypeFactory();
    public static final AldorDefineElementType DEFINE_ELEMENT_TYPE = new AldorDefineElementType();
    public static final FileStubElementType FILE_ELEMENT_TYPE = new FileStubElementType();
    private final Map<String, IElementType> factoryForName = Maps.newHashMap();

    AldorElementTypeFactory() {
        factoryForName.put(".*_Define", DEFINE_ELEMENT_TYPE);
        factoryForName.put("FILE", FILE_ELEMENT_TYPE);
    }

    public static IElementType createElement(String name) {
        return instance.createElementImpl(name);
    }

    public IElementType createElementImpl(String name) {
        /* Factory should be used once, so can be a bit expensive here */
        for (Map.Entry<String, IElementType> ent: factoryForName.entrySet()) {
            if (name.matches(ent.getKey())) {
                return ent.getValue();
            }
        }
        return new AldorElementType(name);
    }

    public static final class FileStubElementType extends IStubFileElementType<PsiFileStub<PsiFile>> {
        private FileStubElementType() {
            super("aldorFile", AldorLanguage.INSTANCE);
        }
    }
}
