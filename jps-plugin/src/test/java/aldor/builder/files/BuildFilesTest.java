package aldor.builder.files;

import org.junit.Test;

import java.io.File;

import static aldor.builder.files.BuildFiles.buildTargetName;
import static org.junit.Assert.assertEquals;

public class BuildFilesTest {

    @Test
    public void testTarget() {
        assertEquals("out/ao/wibble.ao", buildTargetName(new File("/tmp/foo"), new File("/tmp/foo/wibble.as")));
        assertEquals("out/ao/wibble.ao", buildTargetName(new File("/tmp/foo"), new File("/tmp/foo/zz/wibble.as")));
    }

}