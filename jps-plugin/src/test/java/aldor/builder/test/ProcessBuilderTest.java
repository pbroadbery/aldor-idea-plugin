package aldor.builder.test;

import com.google.common.base.Charsets;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessBuilderTest {

    @Test
    public void testOne() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("echo", "hello");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process process = processBuilder.start();
        Thread.sleep(1000);
        InputStreamReader reader = new InputStreamReader(process.getInputStream(), Charsets.US_ASCII);
        int count;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            count = 0;
            while (bufferedReader.readLine() != null) {
                count++;
            }
        }
        Assert.assertEquals(1, count);
    }


}
