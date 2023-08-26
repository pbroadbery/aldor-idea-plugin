package aldor.builder.jps.autoconf.descriptors;

import aldor.builder.jps.autoconf.StaticExecutionEnvironment;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.builder.jps.module.JpsConfiguredRootFacetExtension;
import aldor.util.InstanceCounter;
import aldor.util.SxForm;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;
import static aldor.util.SxFormUtils.number;

public class BuildStaticModelImpl implements BuildStaticModel {
    private static final Logger LOG = Logger.getInstance(BuildStaticModelImpl.class);
    private final int instanceId = InstanceCounter.instance().next(BuildStaticModelImpl.class);

    @NotNull
    private final StaticExecutionEnvironment executionEnvironment;

    public BuildStaticModelImpl(@NotNull StaticExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
   }

    @Override
    public StaticExecutionEnvironment executionEnvironment() {
        return executionEnvironment;
    }

    @Override
    public String createPhonyTargetId(String homePath, String name) {
        return homePath + "-" + name;
    }

    @Override
    public String id() {
        return "{BuildStaticModel:" + instanceId + "}";
    }

    @Override
    public List<BuildInstanceModel> computeModels(JpsModel jpsModel) {
        Map<File, ConfigRootFacetProperties> propertiesMap = new HashMap<>();
        Map<File, JpsModule> moduleMap = new HashMap<>();
        propertiesForRootMap(jpsModel, propertiesMap, moduleMap);
        List<BuildInstanceModel> models = new ArrayList<>();
        for (var ent: propertiesMap.entrySet()) {
            models.add(new BuildInstanceModelImpl(this, jpsModel, moduleMap.get(ent.getKey()), ent.getKey(), ent.getValue()));
        }
        LOG.info("Created models: " + models.size());
        for (var model: models) {
            LOG.info("- Model: " + model.sxForm().asSExpression());
        }
        return models;
    }

    private void propertiesForRootMap(JpsModel model, Map<File, ConfigRootFacetProperties> propertiesMap, Map<File, JpsModule> moduleMap) {
        for (var module : model.getProject().getModules()) {
            JpsConfiguredRootFacetExtension configRootExtension = JpsConfiguredRootFacetExtension.getExtension(module);
            LOG.info("Root facet " + module.getName() + " --> " + configRootExtension);
            if (configRootExtension == null) {
                continue;
            }
            var rootsList = module.getContentRootsList();
            if ((rootsList == null) || (rootsList.getUrls().size() != 1)) {
                continue;
            }
            var root = JpsPathUtil.urlToFile(rootsList.getUrls().get(0));
            propertiesMap.put(root, configRootExtension.getProperties());
            moduleMap.put(root, module);
        }
    }

    @Override
    public @NotNull SxForm sxForm() {
        return list().add(name("BuildStaticModelImpl")).add(number(instanceId));
    }

}
