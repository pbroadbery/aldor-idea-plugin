package aldor.builder;

import aldor.builder.test.AldorJpsTestCase;
import aldor.builder.test.CompileScopeTestBuilder;
import org.jetbrains.jps.model.module.JpsModule;

import java.util.Collections;

public class JpsAldorBuildTest extends AldorJpsTestCase {

    public void testOne() {
        JpsModule module = addModule("aldor-codebase", Collections.emptyList(), "out");

        createFile("aldor-codebase/foo.as", "X: with == add");
        createFile("aldor-codebase/bar.as", "X: with == add");

        rebuildAll();
        System.out.println("---- BUILD COMPLETE -----");
        createFile("aldor-codebase/bar.as", "Y: with == add");

        doBuild(CompileScopeTestBuilder.make()).assertSuccessful();
    }

}
