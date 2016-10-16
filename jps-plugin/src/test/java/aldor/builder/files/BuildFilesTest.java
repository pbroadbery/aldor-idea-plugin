package aldor.builder.files;

import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BuildFilesTest {

    @Test
    public void testAbsoluteNames() {
        File f = BuildFiles.buildDirectoryForFile(Collections.singletonList(new File("/home/pab/IdeaProjects/aldor-codebase")),
                                new File("/home/pab/IdeaProjects/aldor-codebase/aldor/aldor/lib/comp/src/types/cinfer.as"));
        assertNotNull(f);
        assertEquals("/home/pab/IdeaProjects/aldor-codebase/build/lib/comp/src/types", f.getPath());
    }

}
