package aldor.symbolfile;

import aldor.syntax.Syntax;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Declaration;
import aldor.syntax.components.Id;
import aldor.syntax.components.OtherSx;
import aldor.syntax.components.SxSyntaxRepresentation;
import aldor.util.SExpression;
import aldor.util.SxType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Turns SExpressions into syntax
 */
public final class AnnotationFileUtils {

    @NotNull
    public static Syntax parseSx(AnnotationLookup lookup, SExpression sx) {
        if (sx.isOfType(SxType.Symbol)) {
            return new Id(new SxSyntaxRepresentation<>(sx, sx.symbol()));
        }
        else if (sx.isOfType(SxType.Cons)) {
            if (sx.car().equals(SymbolFileSymbols.Ref)) {
                return parseSx(lookup, lookup.type(sx.cdr().integer()));
            }
            else if (sx.car().equals(SymbolFileSymbols.Apply)) {
                return new Apply(null,
                        sx.cdr().asList().stream().map(elt -> parseSx(lookup, elt)).collect(Collectors.toList()));
            }
            else if (sx.car().equals(SymbolFileSymbols.Id)) {
                SExpression symeSx = sx.cdr().asAssociationList().get(SymbolFileSymbols.Syme);
                if (symeSx != null) {
                    Syme syme = lookup.syme(symeSx.cdr().integer());
                    String name = syme.name();
                    return new Id(new SxSyntaxRepresentation<>(sx, name));
                }
                else {
                    SExpression name = sx.cdr().asAssociationList().get(SymbolFileSymbols.Name);
                    if (name != null) {
                        return new Id(new SxSyntaxRepresentation<>(sx, name.symbol()));
                    }
                    else {
                        return new OtherSx(sx);
                    }
                }
            }
            else if (sx.car().equals(SymbolFileSymbols.Comma)) {
                List<Syntax> args = sx.cdr().asList().stream().map(elt -> parseSx(lookup, elt)).collect(Collectors.toList());
                return new Comma(null, args);
            }
            else if (sx.car().equals(SymbolFileSymbols.Declare)) {
                List<Syntax> args = sx.cdr().asList().stream().map(elt -> parseSx(lookup, elt)).collect(Collectors.toList());
                return new Declaration(null, args);
            }
        }
        else {
            return new OtherSx(sx);
        }
        // Not symbol, not cons.  bizzare.
        return new OtherSx(sx);
    }

}
