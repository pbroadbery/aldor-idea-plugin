package aldor.symbolfile;

import aldor.syntax.Syntax;
import aldor.syntax.components.Other;
import aldor.util.SExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static aldor.symbolfile.PopulatedAnnotationFile.sxToSrcPos;

public class Syme {
        @NotNull
        private final AnnotationLookup lookup;
        @NotNull
        private final SExpression sx;
        private final String name;// Should this be "Symbol" of some sort?

        @Nullable
        private final SrcPos srcpos;

        Syme(@NotNull AnnotationLookup lookup, @NotNull SExpression sx) {
            this.lookup = lookup;
            this.sx = sx;
            Map<SExpression, SExpression> props = sx.asAssociationList();
            this.name = props.get(SymbolFileSymbols.Name).symbol();
            SExpression pos = props.get(SymbolFileSymbols.SrcPos);
            this.srcpos = (pos == null) ? null : sxToSrcPos(pos);
        }

        public Syntax type() {
            SExpression ref = sx.asAssociationList().get(SExpression.symbol("type"));
            if (ref == null) {
                return new Other(null);
            }
            return getSyntax(ref);
        }


        public Syntax exporter() {
            SExpression ref = sx.asAssociationList().get(SExpression.symbol("exporter"));
            if (ref == null) {
                return new Other(null);
            }
            return getSyntax(ref);
        }

        @NotNull
        private Syntax getSyntax(SExpression ref) {
            try {
                return AnnotationFileUtils.parseSx(lookup, ref);
            }
            catch (RuntimeException e) {
                System.out.println("SX: " + sx);
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
    }