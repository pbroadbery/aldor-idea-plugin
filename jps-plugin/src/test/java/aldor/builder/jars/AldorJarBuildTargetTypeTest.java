package aldor.builder.jars;

import aldor.builder.AldorBuilderService;
import aldor.builder.jps.JpsAldorModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsEventDispatcher;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsNamedElement;
import org.jetbrains.jps.model.impl.JpsEventDispatcherBase;
import org.jetbrains.jps.model.impl.JpsModelImpl;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.impl.JpsModuleSourceRootImpl;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AldorJarBuildTargetTypeTest {

    @Test
    public void testJarBuild() throws IOException {
        JpsModel model = createModel();
        AldorBuilderService builderService = new AldorBuilderService();
        AldorJarBuildTargetType jarBuildTargetType = new AldorJarBuildTargetType(builderService);

        @NotNull List<AldorJarBuildTarget> targets = jarBuildTargetType.computeAllTargets(model);

        System.out.println("Targets are: " + targets);
        assertTrue(targets.isEmpty());
    }

    @NotNull
    private JpsModel emptyModel() {
        JpsEventDispatcher dispatcher = dispatcher("model");
        return new JpsModelImpl(dispatcher);
    }
    @NotNull
    private JpsModel createModel() throws IOException {
        JpsModel model = emptyModel();
        File tmpdir = Files.createTempDirectory("silly-module").toFile();
        assertTrue((new File(tmpdir, "foo.as")).createNewFile());
        JpsModel modifiable = model.createModifiableModel(dispatcher("mod-1"));

        JpsModule module = modifiable.getProject().addModule("foo", new JpsAldorModuleType());
        module.addSourceRoot(new JpsModuleSourceRootImpl<>(tmpdir.toURI().toURL().toExternalForm(), JavaSourceRootType.SOURCE,
                JavaSourceRootType.SOURCE.createDefaultProperties()));
        modifiable.commit();
        return model;
    }

    @NotNull
    private JpsEventDispatcher dispatcher(String name) {
        return new JpsEventDispatcherBase() {
            @Override
            public void fireElementRenamed(@NotNull JpsNamedElement element, @NotNull String oldName, @NotNull String newName) {
                System.out.println("Rename: (" + name + ")" + oldName + " --> " + newName);
            }

            @Override
            public void fireElementChanged(@NotNull JpsElement element) {
                System.out.println("Changed: (" + name + ")" + element);
            }
        };
    }
}