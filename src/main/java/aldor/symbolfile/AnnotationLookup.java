package aldor.symbolfile;

import aldor.util.sexpr.SExpression;

interface AnnotationLookup {
    Syme syme(int n);

    SExpression type(int n);

    Iterable<Syme> symes();
}
