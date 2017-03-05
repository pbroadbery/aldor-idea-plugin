package aldor.symbolfile;

import aldor.syntax.Syntax;
import aldor.syntax.components.Other;
import aldor.util.sexpr.SExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static aldor.symbolfile.PopulatedAnnotationFile.sxToSrcPos;
import static java.util.Optional.ofNullable;

public class Syme {
    @NotNull
    private final AnnotationLookup lookup;
    @NotNull
    private final SExpression sx;
    private final String name;// Should this be "Symbol" of some sort?
    private final int typeCode;

    @Nullable
    private final SrcPos srcpos;

    Syme(@NotNull AnnotationLookup lookup, @NotNull SExpression sx) {
        this.lookup = lookup;
        this.sx = sx;
        Map<SExpression, SExpression> props = sx.asAssociationList();
        this.name = props.get(SymbolFileSymbols.Name).symbol();
        SExpression pos = props.get(SymbolFileSymbols.SrcPos);
        this.srcpos = (pos == null) ? null : sxToSrcPos(pos);
        this.typeCode = ofNullable(props.get(SymbolFileSymbols.TypeCode)).map(SExpression::integer).orElse(-1);
    }

    public Syntax type() {
        SExpression ref = sx.asAssociationList().get(SymbolFileSymbols.Type);
        if (ref == null) {
            return new Other(null);
        }
        return getSyntax(ref);
    }

    public int typeCode() {
        return typeCode;
    }

    @Nullable
    public String library() {
        return ofNullable(sx.asAssociationList().get(SymbolFileSymbols.Lib)).map(SExpression::string).orElse(null);
    }

    @NotNull
    public Syntax exporter() {
        return ofNullable(sx.asAssociationList().get(SymbolFileSymbols.Exporter)).map(this::getSyntax).orElse(new Other(null));
    }

    @Nullable
    public Syme original() {
        SExpression ref = sx.asAssociationList().get(SymbolFileSymbols.Original);
        if (ref == null) {
            return null;
        }

        return lookup.syme(ref.cdr().integer());
    }

    @NotNull
    private Syntax getSyntax(SExpression ref) {
        try {
            return AnnotationFileUtils.parseSx(lookup, ref);
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);
            return new Other(null);
        }
    }

    public String name() {
        return name;
    }

    public SrcPos srcpos() {
        return srcpos;
    }

    @Override
    public String toString() {
        return sx.toString();
    }

    public String archiveLib() {
        return ofNullable(sx.asAssociationList().get(SymbolFileSymbols.Exporter))
                .map(x -> x.asAssociationList().get(SymbolFileSymbols.Lib))
                .map(SExpression::string)
                .orElse(null);
    }
}
