package aldor.psi;

import java.util.Objects;

public enum AbbrevClassifier {
    ERR(null), DOM("domain"), CAT("category"), PKG("package");

    public final String name;

    AbbrevClassifier(String name) {
        this.name = name;
    }

    public static AbbrevClassifier forText(String classifierText) {
        for (AbbrevClassifier c : values()) {
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
