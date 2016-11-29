package aldor.builder.test;

import groovy.json.internal.Charsets;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class ProcessBuilderTest {

    @Test
    public void testOne() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("echo", "hello");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process process = processBuilder.start();
        Thread.sleep(1000);
        InputStreamReader reader = new InputStreamReader(process.getInputStream(), Charsets.US_ASCII);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        int count = 0;
        while ( (line = bufferedReader.readLine()) != null) {
            count++;
        }
        assertEquals(1, count);
    }


}
