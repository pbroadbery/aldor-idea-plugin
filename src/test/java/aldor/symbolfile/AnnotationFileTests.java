package aldor.symbolfile;

import aldor.util.SExpression;

import static aldor.symbolfile.SymbolFileSymbols.Lib;
import static aldor.symbolfile.SymbolFileSymbols.Name;
import static aldor.symbolfile.SymbolFileSymbols.Original;
import static aldor.symbolfile.SymbolFileSymbols.Ref;
import static aldor.symbolfile.SymbolFileSymbols.SrcPos;
import static aldor.symbolfile.SymbolFileSymbols.Syme;
import static aldor.symbolfile.SymbolFileSymbols.Type;
import static aldor.symbolfile.SymbolFileSymbols.TypeCode;
import static aldor.util.SExpression.cons;
import static aldor.util.SExpression.integer;
import static aldor.util.SExpression.string;
import static aldor.util.SExpression.symbol;
import static aldor.util.SExpressions.list;

public final class AnnotationFileTests {

    public static SExpression type(int index) {
        return cons(Type, cons(Ref, integer(index)));
    }

    public static SExpression srcpos(String file, int line, int column) {
        return list(SrcPos, string(file), integer(line), integer(column));
    }

    public static SExpression typeCode(int typeCode) {
        return cons(TypeCode, integer(typeCode));
    }

    public static SExpression syme(int i) {
        return cons(Syme, cons(Ref, integer(i)));
    }

    public static SExpression name(String name) {
        return cons(Name, symbol(name));
    }

    public static SExpression original(int index) {
        return cons(Original, cons(Ref, integer(index)));
    }

    public static SExpression lib(String name) {
        return cons(Lib, string(name));
    }

    public static SExpression exporter(int index) {
        return cons(Lib, cons(Ref, integer(index)));
    }

}
