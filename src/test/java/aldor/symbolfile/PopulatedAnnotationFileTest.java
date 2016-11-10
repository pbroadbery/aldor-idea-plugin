package aldor.symbolfile;

import aldor.syntax.Syntax;
import aldor.util.SExpression;
import aldor.util.SymbolPolicy;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertNotNull;

public class PopulatedAnnotationFileTest {

    @Test
    public void testFile() throws FileNotFoundException {
        SExpression sx = SExpression.read(new FileReader("/tmp/sal_array.abn"));

        PopulatedAnnotationFile file = new PopulatedAnnotationFile("array", sx);

        Iterable<Syme> symes = file.symes();

        for (Syme syme: symes) {
            Syntax syntax = syme.type();
            System.out.println("Syme: " + syme.name() + " " + syme.type());
            assertNotNull(syntax);
        }
    }

    @Test
    public void testFile2() throws FileNotFoundException {
        SExpression sx = SExpression.read(new FileReader("/tmp/alg_serpoly.abn"), SymbolPolicy.ALLCAPS);

        PopulatedAnnotationFile file = new PopulatedAnnotationFile("array", sx);

        Iterable<Syme> symes = file.symes();

        for (Syme syme: symes) {
            Syntax syntax = syme.type();
            System.out.println("Syme: " + syme.name() + " " + syme.type());
            assertNotNull(syntax);
        }
    }

}
