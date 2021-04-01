package aldor.sdk.aldor;

import aldor.sdk.IProbeRunner;
import aldor.sdk.OsDetails;
import aldor.test_util.ExecutablePresentRule;
import aldor.util.AnnotatedOptional;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AldorVersionQueryTest {

    @Test
    public void aldorVersionFromOutput() throws Exception {
        AldorVersionQuery query = new AldorVersionQuery();
        assertEquals(AnnotatedOptional.of("12345"), query.aldorVersionFromOutput(Collections.singletonList("Aldor version 12345")));
    }

    @Test
    public void fricasVersionFromOutput() throws Exception {
        AldorVersionQuery query = new AldorVersionQuery();
        assertEquals(AnnotatedOptional.of("12345"),
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

        AnnotatedOptional<String, String> v = query.fricasVersion(r.prefix());

        assertTrue(v.isPresent());
        System.out.println("Version is: " + v.get());
    }

    @Test
    public void testWindowsExecution() {
        RunnerInvocation runner = new RunnerInvocation(AnnotatedOptional.of(Collections.emptyList()));
        AldorVersionQuery query = new AldorVersionQuery(new OsDetails(true), runner);
        assertEquals("c:/fricas/bin/FRICASsys", query.fricasPath("c:/fricas"));
        assertEquals(AnnotatedOptional.missing("No output from fricas command"), query.fricasVersion("c:/fricas"));
    }

    @Test
    public void testExtractVersion() {
        String text =
                "                       FriCAS Computer Algebra System \n" +
                "                         Version: FriCAS 2020-04-23\n" +
                "                   Timestamp: Mon 22 Feb 23:01:28 GMT 2021\n" +
                "-----------------------------------------------------------------------------\n" +
                "   Issue )copyright to view copyright notices.\n" +
                "   Issue )summary for a summary of useful system commands.\n" +
                "   Issue )quit to leave FriCAS and return to shell.\n" +
                "-----------------------------------------------------------------------------\n" +
                " \n";
        RunnerInvocation invocation = new RunnerInvocation(AnnotatedOptional.of(Arrays.asList(text.split("\n"))));
        AldorVersionQuery query = new AldorVersionQuery(new OsDetails(true), invocation);
        assertEquals("c:/fricas/bin/FRICASsys", query.fricasPath("c:/fricas"));
        assertEquals(AnnotatedOptional.of("FriCAS 2020-04-23"), query.fricasVersion("c:/fricas"));
    }

    static class RunnerInvocation implements IProbeRunner {
        List<String> command = null;
        Map<String, String> env = null;
        AnnotatedOptional<List<String>, String> results;

        RunnerInvocation(AnnotatedOptional<List<String>, String> results) {
            this.results = results;
        }
        @Override
        public AnnotatedOptional<List<String>, String> readProcessOutput(List<String> command, Map<String, String> environment) {
            this.command = command;
            this.env = new HashMap<>(environment);
            return results;
        }
    }
}
