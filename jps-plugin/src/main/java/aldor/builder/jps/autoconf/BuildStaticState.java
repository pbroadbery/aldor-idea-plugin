package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.BuildInstanceModel;
import aldor.builder.jps.autoconf.descriptors.BuildStaticModel;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.jps.model.JpsModel;

import java.util.Collection;
import java.util.List;

// TODO: Interface
public class BuildStaticState {
    private static final Logger LOG = Logger.getInstance(BuildStaticState.class);

    private static final BuildStaticState instance = new BuildStaticState();
    private BuildStaticModel staticModel = null;
    private List<BuildInstanceModel> instanceModelList = null;
    private StaticExecutionEnvironment executionEnvironment = null;
    private JpsModel jpsModel = null;

    private BuildStaticState() {
    }

    public void setStaticState(BuildStaticModel staticModel, StaticExecutionEnvironment staticExecutionEnvironment) {
        this.staticModel = staticModel;
        this.executionEnvironment = staticExecutionEnvironment;
        this.instanceModelList = null;

    }

    public StaticExecutionEnvironment executionEnvironment() {
        return executionEnvironment;
    }

    public void setInstanceModels(List<BuildInstanceModel> models) {
        if (models != null) {
            throw new IllegalStateException("Models should be null");
        }
        instanceModelList = models;
    }

    public void resetModelList() {
        if (instanceModelList == null) {
            throw new IllegalStateException("Models should not be null");
        }
        instanceModelList.forEach(model -> model.close());
        instanceModelList = null;
    }

    public static BuildStaticState instance() {
        return instance;
    }

    public Collection<BuildInstanceModel> instanceModels() {
        return instanceModelList;
    }

    public Collection<BuildInstanceModel> updateJpsModel(JpsModel jpsModel) {
        if (jpsModel != this.jpsModel) {
            List<BuildInstanceModel> models = staticModel.computeModels(jpsModel);
            this.instanceModelList = models;
            this.jpsModel = jpsModel;
            LOG.info("Updating Static state - model changed! - " + instanceModelList.size() + " models found");
        }
        return instanceModelList;
    }
}
