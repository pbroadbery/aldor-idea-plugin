package pab.aldor;

import aldor.AldorLexerAdapter;
import aldor.Linearise;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class LineariseTest {

    @Test
    public void testSingleSection() {
        String text = "#pile\nFoo == with\n a = 1\n b = \"2\"\nBar == add";
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart(text);
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        for (Linearise.PiledSection ps: pp) {
            ps.showIndents();
        }

        lineariser.blockMarkers(pp.get(0));
        System.out.println("Blocks: " + pp.get(0).blockMarkers());
        assertEquals(1, pp.size());
        assertEquals(4, pp.get(0).lines().size());
    }

    @Test
    public void testPileNone() {
        String text = "Foo == with {\n a = 1; \n b = \"2\"}\nBar == X";
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart(text);
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        assertTrue(pp.isEmpty());
    }

    @Test
    public void testNoCodeAfterPile() {
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart("#pile\n");
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        for (Linearise.PiledSection ps: pp) {
            ps.showIndents();
        }
        assertTrue(pp.isEmpty());
    }


    @Test
    public void testNothingAfterPile() {
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart("#pile");
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        assertTrue(pp.isEmpty());
    }

    @Test
    public void testEmptyPileBlock() {
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart("#pile\n#endpile\n");
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        for (Linearise.PiledSection ps: pp) {
            System.out.println("PS: " + ps);
            ps.showIndents();
        }
        assertTrue(pp.isEmpty());
    }
}
