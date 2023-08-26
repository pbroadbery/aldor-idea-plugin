package aldor.builder;

import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class AldorBuildHelper {


    List<BuildTarget<?>> structurePrerequisites(BuildTargetRegistry registry, File rootDirectory) {
        return Collections.emptyList();
    }
    List<BuildTarget<?>> libraryBuildPrerequisites(BuildTargetRegistry registry) {
        return Collections.emptyList();
    }

}
