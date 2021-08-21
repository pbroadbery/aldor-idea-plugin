package aldor.include;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SysCommandSearchTest {

    @Test
    public void testSimpleSearch() {
        List<String> texts = new ArrayList<>();
        SysCommandSearch.instance().searchIncludes("--foo\n#include \"wibble\"\nfoo\n", seq -> texts.add(seq.toString()));
        assertEquals(List.of("wibble"), texts);
    }

    @Test
    public void testMultiple() {
        List<String> texts = new ArrayList<>();
        SysCommandSearch.instance().searchIncludes("--foo\n#include \"wibble\"\n#include \"foo.as\"\n", seq -> texts.add(seq.toString()));
        assertEquals(List.of("wibble", "foo.as"), texts);
    }

}