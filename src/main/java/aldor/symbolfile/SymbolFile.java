package aldor.symbolfile;

import aldor.util.SExpression;
import aldor.util.SxType;

import java.util.ArrayList;
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

    public String fileName() {
        return fileName;
    }

    private List<AldorNamedObject> parse(SExpression content) {
        List<AldorNamedObject> list = new ArrayList<>();
        for (SExpression sx: content.asList()) {
            if (sx.car().equals(SymbolFileSymbols.Declare)) {
                AldorNamedObject item = parseTopLevelDeclaration(sx.car());
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    private AldorNamedObject parseTopLevelDeclaration(SExpression decl) {
        SExpression name = decl.nth(1);
        SExpression type = decl.nth(2);
        SExpression properties = decl.nth(3);

        SymbolClassifier kind = classifyDefinition(type, properties);
        if (name.isOfType(SxType.Symbol)) {
            return kind.create(name.symbol(), type, properties);
        }
        else {
            return null;
        }
    }

    private SymbolClassifier classifyDefinition(SExpression type, SExpression properties) {
        if (type.isOfType(SxType.Symbol)) {
            return ClassifierModel.Type;
        }
        if (type.isOfType(SxType.Cons)) {
            if (type.car().equals(SymbolFileSymbols.Apply)) {
                return classifyApplication(type.cdr(), properties);
            }
        }
        // FIXME: Should log something here
        return ClassifierModel.Other;
    }

    private SymbolClassifier classifyApplication(SExpression applyArgs, SExpression properties) {
        SExpression operator = applyArgs.nth(0);
        if (operator.equals(SymbolFileSymbols.MapsTo)) {
            return classifyMapping(applyArgs.cdr(), properties);
        }
        return ClassifierModel.Other;
    }

    private SymbolClassifier classifyMapping(SExpression args, SExpression properties) {
        SExpression fromArgs = args.nth(0);
        SExpression toArgs = args.nth(1);

        if (toArgs.isOfType(SxType.Cons) && toArgs.car().equals(SymbolFileSymbols.Define)) {
            SExpression value = toArgs.nth(2);
            if (value.car().equals(SymbolFileSymbols.With)) {
                return ClassifierModel.Category;
            }
            else if (value.car().equals(SymbolFileSymbols.Add)) {
                return ClassifierModel.Domain;
            }
        }
        return ClassifierModel.Other;
    }

    public static class ClassifierModel {
        public static final SymbolClassifier Type = new AnyClassifier("Type");
        public static final SymbolClassifier Function = new AnyClassifier("Function");
        public static final SymbolClassifier Domain = new AnyClassifier("Domain");
        public static final SymbolClassifier Category = new AnyClassifier("Category");
        public static final SymbolClassifier Constant = new AnyClassifier("Constant");
        public static final SymbolClassifier Other = new AnyClassifier("Other");
    }

    abstract static class SymbolClassifier {
        final String name;

        protected SymbolClassifier(String name) {
            this.name = name;
        }

        public abstract AldorNamedObject create(String name, SExpression type, SExpression properties);
    }

    static class AnyClassifier extends SymbolClassifier {
        AnyClassifier(String classifierName) {
            super(classifierName);
        }

        @Override
        public AldorNamedObject create(String name, SExpression type, SExpression properties) {
            SExpression srcpos = properties.asAssociationList().get(SymbolFileSymbols.SrcPos);
            int lineNumber;
            if ((srcpos == null) || !srcpos.isOfType(SxType.Integer)) {
                lineNumber = -1;
            }
            else {
                lineNumber = srcpos.integer();
            }
            return new Other(this, name, type, lineNumber);
        }
    }

    abstract static class AldorNamedObject {
        private final SymbolClassifier classifier;
        private final String name = null;
        private SExpression definition = null;
        private int lineNumber = 0;

        protected AldorNamedObject(SymbolClassifier classifier, String name, SExpression definition, int lineNumber) {
            this.classifier = classifier;
            this.definition = definition;
            this.lineNumber = lineNumber;
        }

        public String name() {
            return name;
        }
    }

    static class Domain extends AldorNamedObject {

        protected Domain(SymbolClassifier classifier, String name, SExpression definition, int lineNumber) {
            super(classifier, name, definition, lineNumber);
        }
    }


    static class Category extends AldorNamedObject {
        protected Category(SymbolClassifier classifier, String name, SExpression definition, int lineNumber) {
            super(classifier, name, definition, lineNumber);
        }
    }


    static class Type extends AldorNamedObject {

        protected Type(SymbolClassifier classifier, String name, SExpression definition, int lineNumber) {
            super(classifier, name, definition, lineNumber);
        }
    }


    static class Other extends AldorNamedObject {

        public Other(SymbolClassifier functionClassifier, String name, SExpression definition, int lineNumber) {
            super(functionClassifier, name, definition, lineNumber);
        }
    }

}
