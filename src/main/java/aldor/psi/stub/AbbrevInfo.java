package aldor.psi.stub;

import aldor.psi.SpadAbbrev;

import java.util.Optional;

/**
 * Created by pab on 21/02/17.
 */
public class AbbrevInfo {
    private final SpadAbbrev.Classifier classifier;
    private final String abbrev;
    private final String name;
    private final int nameIndex;

    public AbbrevInfo(SpadAbbrev.Classifier classifier, String abbrev, String name, int nameIndex) {
        this.classifier = classifier;
        this.abbrev = abbrev;
        this.name = name;
        this.nameIndex = nameIndex;
    }

    public AbbrevInfo() {
        this(SpadAbbrev.Classifier.ERR, "", "", -1);
    }

    public boolean isError() {
        return classifier == SpadAbbrev.Classifier.ERR;
    }

    public Optional<AbbrevInfo> maybe() {
        return isError() ? Optional.empty() : Optional.of(this);
    }

    public SpadAbbrev.Classifier kind() {
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
