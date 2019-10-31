package aldor.sdk.aldor;

import aldor.test_util.ExecutablePresentRule;
import aldor.util.AnnotatedOptional;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;


public class AldorVersionQueryTest {

    @Test
    public void aldorVersionFromOutput() throws Exception {
        AldorVersionQuery query = new AldorVersionQuery();
        Assert.assertEquals(AnnotatedOptional.of("12345"), query.aldorVersionFromOutput(Collections.singletonList("Aldor version 12345")));
    }

    @Test
    public void fricasVersionFromOutput() throws Exception {
        AldorVersionQuery query = new AldorVersionQuery();
        Assert.assertEquals(AnnotatedOptional.of("12345"),
                query.fricasVersionFromOutput(Arrays.asList("Some text", "Version: 12345", "More text")));
    }


    @Test
    public void testAldorVersion() {
        ExecutablePresentRule.Aldor r = new ExecutablePresentRule.Aldor();
        AldorVersionQuery query = new AldorVersionQuery();

        AnnotatedOptional<String, String> v = query.aldorVersion(r.executable().getPath());

        assertTrue(v.isPresent());
        System.out.println("Version is: " + v.get());
    }


    @Test
    public void testFricasVersion() {
        ExecutablePresentRule.Fricas r = new ExecutablePresentRule.Fricas();
        AldorVersionQuery query = new AldorVersionQuery();

        AnnotatedOptional<String, String> v = query.fricasVersion(r.executable().getPath());

        assertTrue(v.isPresent());
        System.out.println("Version is: " + v.get());
    }


}
