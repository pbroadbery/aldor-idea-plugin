package aldor.builder.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ProcessBuilderTest {

    @Test
    public void testOne() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("echo", "hello\nworld");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process process = processBuilder.start();
        Thread.sleep(1000);
        InputStreamReader reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
        int count;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            count = 0;
            while (bufferedReader.readLine() != null) {
                count++;
            }
        }
        int result = process.waitFor();
        Assert.assertEquals(0, result);
        Assert.assertEquals(2, count);
    }
}
