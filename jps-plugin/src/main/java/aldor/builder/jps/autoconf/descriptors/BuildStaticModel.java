package aldor.builder.jps.autoconf.descriptors;

import aldor.builder.jps.autoconf.StaticExecutionEnvironment;
import aldor.util.HasSxForm;
import org.jetbrains.jps.model.JpsModel;

import java.util.List;

// Independent of current build
public interface BuildStaticModel extends HasSxForm {
    // "std-static-1"
    String id();

    List<BuildInstanceModel> computeModels(JpsModel jpsModel);

    StaticExecutionEnvironment executionEnvironment();

    String createPhonyTargetId(String homePath, String configured);
}
