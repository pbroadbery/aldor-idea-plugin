package aldor.psi;

import aldor.psi.stub.AbbrevInfo;
import aldor.psi.stub.SpadAbbrevStub;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;

import java.util.Objects;

public interface SpadAbbrev extends StubBasedPsiElement<SpadAbbrevStub>, Navigatable {

    AbbrevInfo abbrevInfo();

    PsiElement setName(String newElementName);

    enum Classifier {
        ERR(null), DOM("domain"), CAT("category"), PKG("package");

        public final String name;

        Classifier(String name) {
            this.name = name;
        }

        public static Classifier forText(String classifierText) {
            for (Classifier c : values()) {
                if ((c.name != null) && Objects.equals(c.value(), classifierText)) {
                    return c;
                }
            }
            return null;
        }

        public String value() {
            return name;
        }
    }
}
