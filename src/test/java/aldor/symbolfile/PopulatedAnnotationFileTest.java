package aldor.symbolfile;

import aldor.syntax.Syntax;
import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SExpressions;
import aldor.util.sexpr.SymbolPolicy;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertNotNull;

public class PopulatedAnnotationFileTest {

    @Test
    @Ignore
    public void testFile() throws FileNotFoundException {
        @SuppressWarnings("ImplicitDefaultCharsetUsage")
        SExpression sx = SExpressions.read(new FileReader("/tmp/sal_array.abn"));

        PopulatedAnnotationFile file = new PopulatedAnnotationFile("array", sx);

        Iterable<Syme> symes = file.symes();

        for (Syme syme: symes) {
            Syntax syntax = syme.type();
            System.out.println("Syme: " + syme.name() + " " + syme.type());
            assertNotNull(syntax);
        }
    }

    @Test
    @Ignore
    public void testFile2() throws FileNotFoundException {
        @SuppressWarnings("ImplicitDefaultCharsetUsage")
        SExpression sx = SExpressions.read(new FileReader("/tmp/alg_serpoly.abn"), SymbolPolicy.ALLCAPS);

        PopulatedAnnotationFile file = new PopulatedAnnotationFile("array", sx);

        Iterable<Syme> symes = file.symes();

        for (Syme syme: symes) {
            Syntax syntax = syme.type();
            System.out.println("Syme: " + syme.name() + " " + syme.type());
            assertNotNull(syntax);
        }
    }

}
