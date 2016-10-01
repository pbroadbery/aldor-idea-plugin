package aldor.symbolfile;

import aldor.util.SExpression;

import java.util.Collection;
import java.util.List;

public class SymbolFile {
    private final String fileName;
    private final SExpression content;
    private List<AldorNamedObject> topLevel = null;

    SymbolFile(String fileName, SExpression content) {
        this.fileName = fileName;
        this.content = content;
    }

    Collection<AldorNamedObject> topLevel() {
        if (topLevel == null) {
            topLevel = parse(content);
        }
        return topLevel;
    }

    private List<AldorNamedObject> parse(SExpression content) {
        return  null;
    }


    abstract static class AldorNamedObject {
        private final String name = null;
        private SExpression definition = null;
        private int lineNumber = 0;

        public String name() {
            return name;
        }
    }

    static class Domain extends AldorNamedObject {

    }


    static class Category extends AldorNamedObject {

    }


    static class Type extends AldorNamedObject {

    }


}
