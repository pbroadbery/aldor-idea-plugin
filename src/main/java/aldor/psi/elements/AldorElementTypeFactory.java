package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.language.SpadLanguage;
import com.google.common.collect.Maps;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;

import java.util.Map;

/**
 * Aldor element types
 */
public class AldorElementTypeFactory {
    private static final AldorStubFactory stubFactory = new AldorStubFactoryImpl();
    public static final IElementType DEFINE_ELEMENT_TYPE = new AldorDefineElementType(stubFactory);
    public static final IElementType SPAD_ABBREV_ELEMENT_TYPE = new SpadAbbrevElementType(stubFactory);
    public static final FileStubElementType ALDOR_FILE_ELEMENT_TYPE = new FileStubElementType(AldorLanguage.INSTANCE);
    public static final FileStubElementType SPAD_FILE_ELEMENT_TYPE = new FileStubElementType(SpadLanguage.INSTANCE);
    private static final AldorElementTypeFactory instance = new AldorElementTypeFactory();

    private final Map<String, IElementType> factoryForName = Maps.newHashMap();

    AldorElementTypeFactory() {
        factoryForName.put(".*_DEFINE", DEFINE_ELEMENT_TYPE);
        factoryForName.put("SPAD_ABBREV", SPAD_ABBREV_ELEMENT_TYPE);
        factoryForName.put("ALDOR_FILE", ALDOR_FILE_ELEMENT_TYPE);
        factoryForName.put("SPAD_FILE", SPAD_FILE_ELEMENT_TYPE);
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
        private FileStubElementType(Language language) {
            super("aldorFile", language);

        }

        @Override
        public int getStubVersion() {
            return stubFactory.getVersion();
        }

    }
}
