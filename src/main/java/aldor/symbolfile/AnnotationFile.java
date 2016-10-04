package aldor.symbolfile;

import aldor.syntax.Syntax;
import aldor.util.SExpression;
import aldor.util.SxType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * In memory version of an abn file.
 */
public class AnnotationFile {
    private final String fileName;
    private final SExpression sx;
    private Map<SrcPos, SExpression> sxForSrcPos = null;
    private List<SExpression> symes;
    private List<SExpression> types;

    public AnnotationFile(String fileName, SExpression sx) {
        this.fileName = fileName;
        this.sx = sx;
    }

    void parseForIds(Map<SrcPos, SExpression> map, SExpression sx) {
        for (SExpression child: sx.asList()) {
            if (child.isOfType(SxType.Cons) && child.car().equals(SymbolFileSymbols.Id)) {
                SrcPos pos = srcPosForId(child);
                if (pos != null) {
                    map.put(pos, child);
                }
            }
            else if (sx.isOfType(SxType.Cons)) {
                parseForIds(map, child);
            }
        }
    }


    Syntax type(SExpression idSx) {
        return null;
    }

    SrcPos srcPosForId(SExpression sx) {
        SExpression pos = sx.asAssociationList().get(SymbolFileSymbols.SrcPos);
        return new SrcPos(pos.nth(1).integer(), pos.nth(2).integer());
    }


    @Nullable
    Location findLocation(SrcPos srcPos) {
        SExpression props = sxForSrcPos.get(srcPos);
        if (props == null) {
            return null;
        }

        SExpression symeSx = symes.get(props.asAssociationList().get(SymbolFileSymbols.Ref).integer());
        return null;
    }

    private static class SrcPos {
        private final int lineNumber;
        private final int columnNumber;

        private SrcPos(int lineNumber, int columnNumber) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }
    }

    private static class Location {
     //   private final String fileName;
     //   private final SrcPos srcPos;
    }

}
