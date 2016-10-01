package aldor.lexer;

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

        assertEquals(1, pp.size());
        Linearise.PiledSection section = pp.get(0);
        assertEquals(4, section.lines().size());
        lineariser.blockMarkers(section);
        System.out.println("Blocks: " + section.blockMarkers());
        //assertEquals(4, (int) section.blockMarkers().get(0));
        assertEquals(3, (int) section.blockMarkers().get(1));
    }

    @Test
    public void testNestedSection() {
        String text = "#pile\nfoo() == \n x := \n  3\n  4\n x+1\nBar == add";
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart(text);
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        assertEquals(1, pp.size());
        Linearise.PiledSection section = pp.get(0);
        assertEquals(6, section.lines().size());
        lineariser.blockMarkers(section);
        System.out.println("Blocks: " + section.blockMarkers());
        assertEquals(2, section.blockMarkers().size());
        //assertEquals(6, (int) section.blockMarkers().get(0));
        assertEquals(5, (int) section.blockMarkers().get(1));
        assertEquals(4, (int) section.blockMarkers().get(2));
    }


    @Test
    public void testBlankLines() {
        AldorLexerAdapter lla = AldorLexerAdapter.createAndStart("#pile\nrepeat\n Statement1\n\n Statement2\n");
        Linearise lineariser = new Linearise();
        List<Linearise.PiledSection> pp = lineariser.scanForPiledSections(lla);
        assertEquals(1, pp.size());
        Linearise.PiledSection section = pp.get(0);
        System.out.println("Blocks: " + section.blockMarkers());
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
        assertTrue(pp.isEmpty());
    }

}
