package aldor.symbolfile;

import aldor.util.SExpression;
import aldor.util.SxType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * In memory version of an abn file.
 */
public class PopulatedAnnotationFile implements AnnotationFile {
    private final String fileName;
    private final NavigableMap<SrcPos, SExpression> sxForSrcPos;
    @NotNull
    private final Lookup lookup;

    public PopulatedAnnotationFile(String fileName, SExpression sx) {
        this.sxForSrcPos = new TreeMap<>();
        this.fileName = fileName;
        parseForIds(sxForSrcPos, sx.car());
        this.lookup = new Lookup(sx.asList().get(1), sx.asList().get(2));
    }

    public Syme syme(int index) {
        return lookup.syme(index);
    }

    public Iterable<Syme> symes() {
        return lookup.symes();
    }

    private static class Lookup implements AnnotationLookup {
        private final List<Syme> symes;
        private final List<SExpression> types;

        Lookup(SExpression symesSx, SExpression typesSx) {
            symes = parseSymes(symesSx);
            types = new ArrayList<>(typesSx.asList());
        }

        @Override
        public Syme syme(int n) {
            return symes.get(n);
        }

        @Override
        public SExpression type(int n) {
            return types.get(n);
        }

        @Override
        public Iterable<Syme> symes() {
            return Collections.unmodifiableList(symes);
        }

        @NotNull
        private List<Syme> parseSymes(SExpression symeList) {
            return symeList.asList().stream().map((sx1) -> new Syme(this, sx1)).collect(Collectors.toList());
        }

    }

    void parseForIds(Map<SrcPos, SExpression> map, SExpression sx) {
        for (SExpression child: sx.asList()) {
            if (child.isOfType(SxType.Cons) && child.car().equals(SymbolFileSymbols.Id)) {
                SExpression sxPos = child.cdr().asAssociationList().get(SymbolFileSymbols.SrcPos);
                if (sxPos != null) {
                    SrcPos pos = sxToSrcPos(sxPos);
                    map.put(pos, child);
                }
            }
            else if (child.isOfType(SxType.Cons)) {
                parseForIds(map, child);
            }
        }
    }

    @NotNull
    static SrcPos sxToSrcPos(@NotNull SExpression pos) {
        return new SrcPos(pos.nth(0).string(), pos.nth(1).integer(), pos.nth(2).integer());
    }

    public Iterable<Map.Entry<SrcPos, SExpression>> entries() {
        return sxForSrcPos.entrySet();
    }

    @Override
    public String sourceFile() {
        return fileName;
    }

    @Nullable
    @Override
    public String errorMessage() {
        return null;
    }

    @Override
    @Nullable
    public Syme lookupSyme(SrcPos srcPos) {
        SExpression sx = sxForSrcPos.get(srcPos);
        if (sx == null) {
            return null;
        }

        SExpression indexRef = sx.cdr().asAssociationList().get(SymbolFileSymbols.Syme);
        if (indexRef == null) {
            return null;
        }
        SExpression index = indexRef.cdr();
        if (!index.isOfType(SxType.Integer)) {
            return null;
        }
        return lookup.syme(index.integer());
    }

}
