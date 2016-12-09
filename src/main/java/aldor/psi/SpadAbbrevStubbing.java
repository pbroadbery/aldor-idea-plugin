package aldor.psi;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

import java.util.Objects;
import java.util.Optional;

public class SpadAbbrevStubbing {
    public enum Classifier {
        ERR(null), DOM("domain"), CAT("category"), PKG("package");

        public final String name;

        Classifier(String name) {
            this.name = name;
        }

        public static Classifier forText(String classifierText) {
            for (Classifier c: values()) {
                if ((c.name != null) && Objects.equals(c.value(), classifierText)) {
                    return c;
                }
            }
            return null;
        }

        private String value() {
            return name;
        }
    }

    public interface SpadAbbrev extends StubBasedPsiElement<SpadAbbrevStub>, Navigatable {
        SpadAbbrevStub createStub(IStubElementType<SpadAbbrevStub, SpadAbbrev> elementType, StubElement<?> parentStub);

        AbbrevInfo abbrevInfo();

        PsiElement setName(String newElementName);
    }

    public interface SpadAbbrevStub extends StubElement<SpadAbbrev> {
        AbbrevInfo info();

        SpadAbbrev createPsi(IStubElementType<SpadAbbrevStub, SpadAbbrev> elementType);
    }

    public static class AbbrevInfo {
        private final Classifier classifier;
        private final String abbrev;
        private final String name;
        private final int nameIndex;

        public AbbrevInfo(Classifier classifier, String abbrev, String name, int nameIndex) {
            this.classifier = classifier;
            this.abbrev = abbrev;
            this.name = name;
            this.nameIndex = nameIndex;
        }

        public AbbrevInfo() {
            this(Classifier.ERR, "", "", -1);
        }

        public boolean isError() {
            return classifier == Classifier.ERR;
        }

        public Optional<AbbrevInfo> maybe() {
            return isError() ? Optional.empty() : Optional.of(this);
        }

        public Classifier kind() {
            return classifier;
        }

        public String abbrev() {
            return abbrev;
        }

        public String name() {
            return name;
        }

        public int nameIndex() {
            return nameIndex;
        }
    }
}
