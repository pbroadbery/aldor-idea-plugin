package aldor.psi.stub;

import aldor.psi.AbbrevClassifier;

import java.util.Optional;

public class AbbrevInfo {
    private final AbbrevClassifier classifier;
    private final String abbrev;
    private final String name;
    private final int nameIndex;

    public AbbrevInfo(AbbrevClassifier classifier, String abbrev, String name, int nameIndex) {
        this.classifier = classifier;
        this.abbrev = abbrev;
        this.name = name;
        this.nameIndex = nameIndex;
    }

    public AbbrevInfo() {
        this(AbbrevClassifier.ERR, "", "", -1);
    }

    public boolean isError() {
        return classifier == AbbrevClassifier.ERR;
    }

    public Optional<AbbrevInfo> maybe() {
        return isError() ? Optional.empty() : Optional.of(this);
    }

    public AbbrevClassifier kind() {
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
