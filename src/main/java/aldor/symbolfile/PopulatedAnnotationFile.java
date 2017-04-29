package aldor.symbolfile;

import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SxType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * In memory version of an abn file.
 */
public class PopulatedAnnotationFile implements AnnotationFile {
    private final String fileName;
    private final Multimap<SrcPos, SExpression> sxForSrcPos;
    @NotNull
    private final Lookup lookup;

    public PopulatedAnnotationFile(String fileName, SExpression sx) {
        this.sxForSrcPos = HashMultimap.create(10, 1);
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

    @Override
    @Nullable
    public Syme symeForNameAndCode(String name, int typeCode) {
        return lookup.symeForNameAndCode(name, typeCode);
    }

    private static class Lookup implements AnnotationLookup {
        private final List<Syme> symes;
        private final List<SExpression> types;
        private final Map<String, Syme> symeForNameAndCode;

        Lookup(SExpression symesSx, SExpression typesSx) {
            symes = parseSymes(symesSx);
            types = new ArrayList<>(typesSx.asList());
            symeForNameAndCode = new HashMap<>();
            for (Syme syme: symes) {
                symeForNameAndCode.put(symeKey(syme), syme);
            }
        }

        private String symeKey(Syme syme) {
            return syme.name() + "-" + syme.typeCode();
        }

        private String symeKey(String name, int typeCode) {
            return name + "-" + typeCode;
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

        @Nullable
        public Syme symeForNameAndCode(String name, int typeCode) {
            return symeForNameAndCode.get(symeKey(name, typeCode));
        }
    }

    void parseForIds(Multimap<SrcPos, SExpression> map, SExpression sx) {
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
    public Collection<Syme> lookupSyme(@NotNull SrcPos srcPos) {
        Collection<SExpression> sExpressions = sxForSrcPos.get(srcPos);

        return sExpressions.stream()
                .map(sx -> sx.cdr().asAssociationList().get(SymbolFileSymbols.Syme))
                .filter(Objects::nonNull)
                .map(SExpression::cdr)
                .filter(x -> x.isOfType(SxType.Integer))
                .map(n -> lookup.syme(n.integer()))
                .collect(Collectors.toList());
    }
}
